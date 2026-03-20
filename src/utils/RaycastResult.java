package utils;

import entity.Chunk;
import org.joml.Vector3f;
import org.joml.Vector3i;

public record RaycastResult(Vector3f worldPos, Vector3i chunkPos, Chunk chunk) {

}