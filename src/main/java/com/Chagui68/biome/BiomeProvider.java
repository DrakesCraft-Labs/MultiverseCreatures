package com.Chagui68.biome;

/**
 * Interfaz para proveedores de biomas (equivalente a: com.dfsek.terra.api.world.biome.generation.BiomeProvider)
 * Determina qué bioma debe estar en cada coordenada (x, y, z)
 */
public interface BiomeProvider {
    
    /**
     * Obtener el bioma en una posición específica (x, y, z)
     * 
     * @param x Coordenada X
     * @param y Coordenada Y (altura)
     * @param z Coordenada Z
     * @param seed Semilla del mundo
     * @return El BiomeConfig en esa posición
     */
    BiomeConfig getBiome(int x, int y, int z, long seed);
    
    /**
     * Obtener el bioma en una posición 2D (x, z) - ignora Y
     * Útil para saber qué bioma "principal" está en una columna
     * 
     * @param x Coordenada X
     * @param z Coordenada Z
     * @param seed Semilla del mundo
     * @return El BiomeConfig en esa posición
     */
    default BiomeConfig getBiome(int x, int z, long seed) {
        return getBiome(x, 64, z, seed);
    }
    
    /**
     * Obtener todos los biomas disponibles en el provider
     */
    Iterable<BiomeConfig> getBiomes();
}
