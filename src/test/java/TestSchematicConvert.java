import com.Chagui68.worldgen.NBTReader;
import com.Chagui68.worldgen.SchematicConverter;

import java.io.File;
import java.util.Map;

/**
 * Quick standalone test - run with: java -cp target/classes TestSchematicConvert
 */
public class TestSchematicConvert {
    public static void main(String[] args) throws Exception {
        String inputPath = args.length > 0 ? args[0] : "Schems/!_Toby_tree-from-abfielder.litematic";
        File inputFile = new File(inputPath);
        
        System.out.println("=== NBT Structure Test ===");
        System.out.println("Reading: " + inputFile.getAbsolutePath());
        
        // Step 1: Read raw NBT
        Map<String, Object> nbt = NBTReader.readFile(inputFile);
        System.out.println("Root keys: " + nbt.keySet());
        
        // Print metadata
        Map<String, Object> metadata = NBTReader.getCompound(nbt, "Metadata");
        if (metadata != null) {
            System.out.println("Metadata keys: " + metadata.keySet());
            Map<String, Object> encSize = NBTReader.getCompound(metadata, "EnclosingSize");
            if (encSize != null) {
                System.out.println("Enclosing Size: " + encSize);
            }
        }
        
        // Print regions
        Map<String, Object> regions = NBTReader.getCompound(nbt, "Regions");
        if (regions != null) {
            System.out.println("Regions: " + regions.keySet());
        }
        
        // Step 2: Full conversion
        System.out.println("\n=== Conversion Test ===");
        SchematicConverter.ConversionResult result = SchematicConverter.convert(inputFile);
        System.out.println("Dimensions: " + result.width + "x" + result.height + "x" + result.length);
        System.out.println("Block count: " + result.blockCount);
        
        // Save output
        File outputFile = new File(inputFile.getParent(), inputFile.getName().replaceAll("\\.litematic$", ".json"));
        SchematicConverter.convertAndSave(inputFile, outputFile);
        System.out.println("Saved to: " + outputFile.getAbsolutePath());
        System.out.println("File size: " + outputFile.length() + " bytes");
        
        // Print first 500 chars of output
        String json = result.json;
        System.out.println("\nFirst 500 chars:\n" + json.substring(0, Math.min(500, json.length())));
        
        System.out.println("\n=== SUCCESS ===");
    }
}
