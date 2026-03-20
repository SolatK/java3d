package entity;

import core.World;
import graphics.Mesh;
import graphics.MeshData;
import org.joml.Vector3f;
import utils.ChunkStatus;

import static core.World.CHUNK_SIZE;

public class Chunk extends Renderable{

    private volatile ChunkStatus status = ChunkStatus.EMPTY;
    private MeshData meshData;

    private final short[] data;

    public static int stride = CHUNK_SIZE + 1;


    @Override
    public void updateModelMatrix() {
        // Чанки обычно только перемещаются
        modelMatrix.identity().translate(position);
    }


    public Chunk(int cx, int cy, int cz) {
        this.position = new Vector3f(
                cx * CHUNK_SIZE,
                cy * CHUNK_SIZE,
                cz * CHUNK_SIZE
                );
        this.data = new short[stride * stride * stride];
    }

    public int getDensity(int x, int y, int z) {
        short val = data[x + y * stride + z * stride * stride];
        return val & 0xFF; // Маскируем младшие 8 бит
    }

    public int getDensityGlobal(int x, int y, int z, World world) {

        if (x >= 0 && x < CHUNK_SIZE && y >= 0 && y < CHUNK_SIZE && z >= 0 && z < CHUNK_SIZE) {
            return this.getDensity(x, y, z);
        }
        // Если вышли за границы — запрашиваем у World (чуть медленнее)
        return world.getDensity(this.position.x + x, this.position.y + y, this.position.z + z);
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

    public void updateMesh(Mesh mesh) {
        this.mesh = mesh;
    }

    public void setStatus(ChunkStatus status) {
        this.status = status;
    }

    public ChunkStatus getStatus() {
        return status;
    }

    public MeshData getMeshData() {
        return meshData;
    }

    public void setMeshData(MeshData meshData) {
        this.meshData = meshData;
    }
}
