package adris.altoclef.multiversion;

import net.minecraft.network.message.MessageType;

public class MessageTypeVer {

    //#if MC >= 11904
    public static MessageType getMessageType(MessageType.Parameters parameters) {
    //#else
    //$$ public static MessageType getMessageType(Object obj) {
    //#endif

        //#if MC >= 12005
        if (parameters == null) {
            return null;
        }
        return parameters.type().value();
        //#elseif MC >= 11904
        //$$ if (parameters == null) {
        //$$     Debug.logError("MessageTypeVer.getMessageType called with null parameters in MC>=11904 branch");
        //$$     return null;
        //$$ }
        //$$ return parameters.type();
        //#else
        //$$ Debug.logError("Cannot get message type from params since they do not exist in this version!");
        //$$ throw new IllegalStateException("Cannot get message type from params since they do not exist in this version!");
        //#endif
    }
}
