package bench;

import at.asitplus.io.UVarInt;
import kotlin.ULong;
import kotlin.jvm.internal.DefaultConstructorMarker;
import org.jetbrains.annotations.NotNull;
import org.openjdk.jmh.annotations.*;

public class UVarintBenchmark {
    @State(Scope.Thread)
    public static class DecodeInput {
        public byte[] bytes;
        @Setup(Level.Trial)
        public void setup() {
            bytes = new byte[] { (byte)0xfe, (byte)0xfd, (byte)0xa5, (byte)0x91, (byte)0x97, (byte)0xbb, (byte)0xa3,
                                 (byte)0x9a, (byte)0x6a };
        }
    }
    @Benchmark
    public @NotNull UVarInt benchmarkDecode(@NotNull DecodeInput input) {
        return UVarInt.Companion.fromByteArrayStrict(input.bytes);
    }

    @State(Scope.Thread)
    public static class EncodeInput {
        public UVarInt input;
        @Setup(Level.Trial)
        public void setup() throws ReflectiveOperationException {
            input = UVarInt.class
                    .getConstructor(long.class, DefaultConstructorMarker.class)
                    .newInstance(0x6afe3e31L, null);
        }
    }
    @Benchmark
    public @NotNull byte[] benchmarkEncode(@NotNull EncodeInput input) {
        return input.input.encodeToByteArray();
    }
}
