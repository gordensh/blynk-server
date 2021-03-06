package cc.blynk.server.core.model.widgets;

import cc.blynk.server.core.model.enums.PinType;
import cc.blynk.server.core.model.widgets.controls.SyncOnActivate;
import cc.blynk.utils.JsonParser;
import io.netty.channel.Channel;

import static cc.blynk.server.core.protocol.enums.Command.SYNC;
import static cc.blynk.utils.BlynkByteBufUtil.makeUTF8StringMessage;
import static cc.blynk.utils.StringUtils.BODY_SEPARATOR_STRING;
import static cc.blynk.utils.StringUtils.makeBody;

/**
 * The Blynk Project.
 * Created by Dmitriy Dumanskiy.
 * Created on 02.12.15.
 */
//todo all this should be replaced with 1 Pin field.
public abstract class OnePinWidget extends Widget implements SyncOnActivate {

    public int deviceId;

    public PinType pinType;

    public byte pin = -1;

    public boolean pwmMode;

    public boolean rangeMappingOn;

    public int min;

    public int max;

    public String value;

    protected static String makeHardwareBody(PinType pinType, byte pin, String value) {
        return String.valueOf(pinType.pintTypeChar) + 'w' + BODY_SEPARATOR_STRING + pin + BODY_SEPARATOR_STRING + value;
    }

    @Override
    public void sendSyncOnActivate(Channel appChannel, int dashId) {
        String hardBody = makeHardwareBody();
        if (hardBody != null) {
            String body = makeBody(dashId, deviceId, hardBody);
            appChannel.write(makeUTF8StringMessage(SYNC, 1111, body));
        }
    }


    public String makeHardwareBody() {
        if (pin == -1 || value == null || pinType == null) {
            return null;
        }
        return isPWMSupported() ? makeHardwareBody(PinType.ANALOG, pin, value) : makeHardwareBody(pinType, pin, value);
    }

    @Override
    public boolean updateIfSame(int deviceId, byte pin, PinType type, String value) {
        if (isSame(deviceId, pin, type)) {
            this.value = value;
            return true;
        }
        return false;
    }

    //todo cover with test
    public boolean isSame(int deviceId, byte pin, PinType type) {
        return this.deviceId == deviceId && this.pin == pin && (
                (type == this.pinType) ||
                (this.isPWMSupported() && type == PinType.ANALOG) ||
                (type == PinType.DIGITAL && this.pinType == PinType.ANALOG)
        );
    }

    @Override
    public String getValue(byte pin, PinType type) {
        return value;
    }

    @Override
    public boolean hasValue(String searchValue) {
        return searchValue.equals(value);
    }

    @Override
    public String getJsonValue() {
        if (value == null) {
            return "[]";
        }
        return JsonParser.valueToJsonAsString(value);
    }

    @Override
    public void append(StringBuilder sb, int deviceId) {
        if (this.deviceId == deviceId) {
            append(sb, pin, pinType, getModeType());
        }
    }

    public boolean isPWMSupported() {
        return false;
    }

}
