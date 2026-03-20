package entity;

import graphics.MeshData;
import org.joml.Vector3f;
import utils.ChunkGenerator;
import utils.MeshGenerator;

public class Chunk extends Renderable{

    private final short[] data;

    public static int SIZE = 32;

    public static int stride = SIZE + 1;

    @Override
    public void updateModelMatrix() {
        // Чанки обычно только перемещаются
        modelMatrix.identity().translate(position);
    }


    public Chunk(Vector3f position) {
        this.position = position;
        this.data = new short[stride * stride * stride];
        fillAndGenerate();
    }

    public int getDensity(int x, int y, int z) {
        short val = data[x + y * stride + z * stride * stride];
        return val & 0xFF; // Маскируем младшие 8 бит
    }

    public int getMaterial(int x, int y, int z) {
        short val = data[x + y * stride + z * stride * stride];
        return (val >> 8) & 0xFF; // Сдвигаем и маскируем
    }

    public void setDensity(int x, int y, int z, int density) {
        int index = x + y * stride + z * stride * stride;
        // Очищаем старую плотность (младшие 8 бит) и записываем новую
        // & 0xFF00 оставляет только материал, & 0xFF гарантирует, что плотность не вылезет за 8 бит
        data[index] = (short) ((data[index] & 0xFF00) | (density & 0xFF));
    }

    public void setMaterial(int x, int y, int z, int material) {
        int index = x + y * stride + z * stride * stride;
        // Очищаем старый материал (старшие 8 бит) и записываем новый
        // & 0x00FF оставляет только плотность, << 8 сдвигает материал в нужную позицию
        data[index] = (short) ((data[index] & 0x00FF) | ((material & 0xFF) << 8));
    }

    public void setBoth(int x, int y, int z, int material, int density) {
        int index = x + y * stride + z * stride * stride;
        // Полная перезапись ячейки одним махом
        data[index] = (short) (((material & 0xFF) << 8) | (density & 0xFF));
    }

    public void fillAndGenerate() {
        ChunkGenerator.fillChunk(this);
        MeshData data = MeshGenerator.generateChunkMeshData(this);
        mesh = MeshGenerator.generateMesh(data);
    }
}
