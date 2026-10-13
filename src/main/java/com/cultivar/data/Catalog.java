package com.cultivar.data;

import com.cultivar.Cultivar;
import com.cultivar.CultivarConfig;
import com.cultivar.genetics.Allele;
import com.cultivar.genetics.AlleleTable;
import com.cultivar.genetics.DefaultGenetics;
import com.cultivar.genetics.Gene;
import com.cultivar.genetics.Genome;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.random.RandomGenerator;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public final class Catalog {
    private static final Map<RegistryAccess, Catalog> CACHE = Collections.synchronizedMap(new WeakHashMap<>());
    private static volatile Catalog defaults;

    private final AlleleTable baseTable;
    private volatile AlleleTable scaledTable;
    private final Map<Gene, GeneDefinition> genes;
    private final Map<Identifier, SpeciesDefinition> species;
    private final Map<Identifier, PhBand> phBands;
    private final Map<Identifier, NitrogenFixer> fixers;
    private final Map<Identifier, BlightStrain> strains;

    private Catalog(AlleleTable baseTable, Map<Gene, GeneDefinition> genes, Map<Identifier, SpeciesDefinition> species,
                    Map<Identifier, PhBand> phBands, Map<Identifier, NitrogenFixer> fixers, Map<Identifier, BlightStrain> strains) {
        this.baseTable = baseTable;
        this.genes = genes;
        this.species = species;
        this.phBands = phBands;
        this.fixers = fixers;
        this.strains = strains;
    }

    public static Catalog get(Level level) {
        return get(level.registryAccess());
    }

    public static Catalog get(RegistryAccess access) {
        Catalog cached = CACHE.get(access);
        if (cached == null) {
            cached = build(access);
            CACHE.put(access, cached);
        }
        return cached;
    }

    public static Catalog defaults() {
        Catalog d = defaults;
        if (d == null) {
            Map<Gene, GeneDefinition> genes = new EnumMap<>(Gene.class);
            DefaultData.genes().values().forEach(g -> genes.put(g.gene(), g));
            Map<Identifier, PhBand> bands = new LinkedHashMap<>();
            DefaultData.phBands().values().forEach(b -> bands.put(b.species(), b));
            Map<Identifier, NitrogenFixer> fixers = new LinkedHashMap<>();
            DefaultData.nitrogenFixers().values().forEach(f -> fixers.put(f.species(), f));
            d = new Catalog(DefaultGenetics.table(), genes, DefaultData.species(), bands, fixers, DefaultData.blightStrains());
            defaults = d;
        }
        return d;
    }

    private static Catalog build(RegistryAccess access) {
        Catalog fallback = defaults();
        Optional<Registry<AlleleDefinition>> alleleRegistry = access.lookup(CultivarRegistries.ALLELE);
        AlleleTable table = fallback.baseTable;
        if (alleleRegistry.isPresent() && alleleRegistry.get().size() > 0) {
            List<Allele> alleles = new ArrayList<>();
            for (Map.Entry<ResourceKey<AlleleDefinition>, AlleleDefinition> e : alleleRegistry.get().entrySet()) {
                alleles.add(e.getValue().toAllele(e.getKey().identifier().getPath()));
            }
            try {
                table = new AlleleTable(alleles, 1.0f);
            } catch (IllegalArgumentException ex) {
                Cultivar.LOGGER.error("Invalid allele datapack ({}); falling back to the built-in allele table", ex.getMessage());
            }
        }

        Map<Gene, GeneDefinition> genes = new EnumMap<>(fallback.genes);
        access.lookup(CultivarRegistries.GENE).ifPresent(r -> r.entrySet().forEach(e -> genes.put(e.getValue().gene(), e.getValue())));

        Map<Identifier, SpeciesDefinition> species = new LinkedHashMap<>(fallback.species);
        access.lookup(CultivarRegistries.SPECIES).ifPresent(r -> r.entrySet().forEach(e -> species.put(e.getKey().identifier(), e.getValue())));

        Map<Identifier, PhBand> bands = new LinkedHashMap<>();
        Optional<Registry<PhBand>> bandRegistry = access.lookup(CultivarRegistries.PH_BAND);
        if (bandRegistry.isPresent() && bandRegistry.get().size() > 0) {
            bandRegistry.get().entrySet().forEach(e -> bands.put(e.getValue().species(), e.getValue()));
        }
        fallback.phBands.forEach(bands::putIfAbsent);

        Map<Identifier, NitrogenFixer> fixers = new LinkedHashMap<>();
        Optional<Registry<NitrogenFixer>> fixerRegistry = access.lookup(CultivarRegistries.NITROGEN_FIXER);
        if (fixerRegistry.isPresent()) {
            fixerRegistry.get().entrySet().forEach(e -> fixers.put(e.getValue().species(), e.getValue()));
        } else {
            fixers.putAll(fallback.fixers);
        }

        Map<Identifier, BlightStrain> strains = new LinkedHashMap<>();
        Optional<Registry<BlightStrain>> strainRegistry = access.lookup(CultivarRegistries.BLIGHT_STRAIN);
        if (strainRegistry.isPresent() && strainRegistry.get().size() > 0) {
            strainRegistry.get().entrySet().forEach(e -> strains.put(e.getKey().identifier(), e.getValue()));
        } else {
            strains.putAll(fallback.strains);
        }
        return new Catalog(table, genes, species, bands, fixers, strains);
    }

    public AlleleTable table() {
        float strength = CultivarConfig.isLoaded() ? CultivarConfig.WILD_ALLELE_STRENGTH.get().floatValue() : 1.0f;
        AlleleTable t = scaledTable;
        if (t == null || t.wildStrength() != strength) {
            t = strength == 1.0f ? baseTable : baseTable.withWildStrength(strength);
            scaledTable = t;
        }
        return t;
    }

    public GeneDefinition gene(Gene gene) {
        return genes.getOrDefault(gene, GeneDefinition.defaultFor(gene));
    }

    public SpeciesDefinition species(Identifier id) {
        SpeciesDefinition def = species.get(id);
        if (def != null) {
            return def;
        }
        CropSpecies builtin = CropSpecies.byKey(id);
        return builtin != null ? builtin.defaultDefinition() : CropSpecies.WHEATLINE.defaultDefinition();
    }

    public SpeciesDefinition species(CropSpecies species) {
        return species(species.key());
    }

    public PhBand phBand(Identifier species) {
        PhBand band = phBands.get(species);
        return band != null ? band : new PhBand(species, 5.5f, 7.5f, 1.5f);
    }

    public Optional<NitrogenFixer> nitrogenFixer(Identifier species) {
        return Optional.ofNullable(fixers.get(species));
    }

    public Map<Identifier, BlightStrain> strains() {
        return strains;
    }

    public Optional<BlightStrain> strain(Identifier id) {
        return Optional.ofNullable(strains.get(id));
    }

    public List<Identifier> strainsFor(Identifier species) {
        List<Identifier> out = new ArrayList<>();
        strains.forEach((id, strain) -> {
            if (strain.infects(species)) {
                out.add(id);
            }
        });
        return out;
    }

    public Genome naturalGenome(Identifier speciesId, RandomGenerator random) {
        SpeciesDefinition def = species(speciesId);
        int poolSize = CultivarConfig.isLoaded() ? CultivarConfig.ALLELES_PER_GENE_PER_SPECIES.get() : 3;
        AlleleTable t = table();
        byte[] codes = new byte[Genome.BYTES];
        for (Gene gene : Gene.VALUES) {
            List<Allele> pool = new ArrayList<>();
            for (String id : def.naturalAlleles().getOrDefault(gene, List.of())) {
                Allele a = t.byId(id);
                if (a != null && a.gene() == gene && !a.wild() && !a.mutationOnly()) {
                    pool.add(a);
                }
                if (pool.size() >= poolSize) {
                    break;
                }
            }
            if (pool.isEmpty()) {
                for (Allele a : t.alleles(gene)) {
                    if (!a.wild() && !a.mutationOnly()) {
                        pool.add(a);
                        break;
                    }
                }
            }
            if (pool.isEmpty()) {
                pool.add(t.alleles(gene).get(0));
            }
            codes[gene.ordinal() * 2] = (byte) pool.get(random.nextInt(pool.size())).code();
            codes[gene.ordinal() * 2 + 1] = (byte) pool.get(random.nextInt(pool.size())).code();
        }
        return Genome.of(codes);
    }

    public Genome wildGenome(Identifier speciesId, RandomGenerator random) {
        Genome natural = naturalGenome(speciesId, random);
        AlleleTable t = table();
        byte[] codes = natural.toBytes();
        for (Gene gene : Gene.VALUES) {
            int wildCode = -1;
            for (Allele a : t.alleles(gene)) {
                if (a.wild()) {
                    wildCode = a.code();
                    break;
                }
            }
            if (wildCode < 0) {
                continue;
            }
            codes[gene.ordinal() * 2] = (byte) wildCode;
            if (random.nextFloat() < 0.85f) {
                codes[gene.ordinal() * 2 + 1] = (byte) wildCode;
            }
        }
        return Genome.of(codes);
    }
}
