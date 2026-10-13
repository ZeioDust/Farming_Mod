package com.cultivar.data;

import com.cultivar.genetics.Gene;
import com.cultivar.genetics.Genome;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.netty.buffer.ByteBuf;
import java.nio.ByteBuffer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public final class GeneticsCodecs {
    private GeneticsCodecs() {
    }

    public static final Codec<Gene> GENE = Codec.STRING.comapFlatMap(id -> {
        Gene gene = Gene.byId(id);
        return gene != null ? DataResult.success(gene) : DataResult.error(() -> "Unknown gene: " + id);
    }, Gene::id);

    public static final Codec<Genome> GENOME = Codec.BYTE_BUFFER.comapFlatMap(buffer -> {
        ByteBuffer copy = buffer.duplicate();
        if (copy.remaining() != Genome.BYTES) {
            return DataResult.error(() -> "Genome must be " + Genome.BYTES + " bytes, got " + copy.remaining());
        }
        byte[] bytes = new byte[Genome.BYTES];
        copy.get(bytes);
        return DataResult.success(Genome.of(bytes));
    }, genome -> ByteBuffer.wrap(genome.toBytes()));

    public static final StreamCodec<ByteBuf, Genome> GENOME_STREAM = ByteBufCodecs.byteArray(Genome.BYTES)
            .map(Genome::of, Genome::toBytes);
}
