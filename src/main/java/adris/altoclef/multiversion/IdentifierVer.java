package adris.altoclef.multiversion;

import net.minecraft.util.Identifier;

public class IdentifierVer {


    @Pattern
    private static Identifier newCreation(String str) {
        if (str == null) {
        }
        //#if MC >= 12100
        return Identifier.of(str);
        //#else
        //$$ return new Identifier(str);
        //#endif
    }


}
