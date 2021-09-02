package no.nordicsemi.android.mesh;

public class MeshTAITime {

    private final Integer taiSeconds;
    private final byte subSecond;
    private final byte uncertainty;
    private final boolean timeAuthority;
    private final short utcDelta;
    private final byte timeZoneOffset;

    /**
     * Time Set is an acknowledged message used to set the Time state of an element (see Section 5.1.1).
     * The response to the Time Set message is a Time Status message.
     *
     * @param taiSeconds     The TAI Seconds state is the current TAI time in seconds after the epoch 2000-01-01T00:00:00 TAI (1999-12-31T23:59:28 UTC).
     *                       For example, the value 0x20E5369D represents the 2017-06-27T15:30:37 TAI (15:30:00 UTC). When an element does not know the time, a special value of 0x0000000000 is used.
     * @param subSecond      The SubSecond is a fractional part of the TAI time, in units of 1/256th seconds. An implementation may increment this field by more than one unit (i.e. the mechanism it uses may have a larger granularity) and/or by different amounts at each increment.
     * @param uncertainty    The Uncertainty field represents the accumulated uncertainty of the Mesh Timestamp in 10- millisecond steps. It includes the Uncertainty of the time source and the accumulated uncertainty resulting from the local clock drift since the last update. Value 255 represents uncertainty of 2.55 seconds or more.
     * @param timeAuthority  The Time Authority bit represents whether the element has a reliable source of TAI, such as a GPS receiver or an NTP-synchronized clock. Time Authority = 1 is when the device itself has access to a reliable and trusted time source, such as NTP or GPS, or has been given the TAI time by a Provisioner (using the Time Setup Server model and a DevKey – see Section 5.3.2).
     *                       Note: Many time sources do not provide TAI directly, but it can be derived from UTC (if the current TAI-UTC Delta is known) or from GPS time (which is always 19 seconds behind TAI).
     * @param utcDelta       The TAI-UTC Delta Current state represents the value: current_TAI minus current_UTC. For example, on 2017-01-19, this value equals +37. The valid range is -255 through +32512 (i.e., 0x00FF represents a value of 0 and 0x7FFF represents a value of 32512).
     * @param timeZoneOffset The Time Zone Offset New state reflects the information on the upcoming Time Zone change. This usually informs about an upcoming Daylight Saving Time change or other change planned by local or regional regulatory bodies. By being aware of the upcoming change, devices can automatically execute the change even without the presence of a change coordinator.
     *                       The Time Zone Offset New field represents the new zone offset in 15-minute increments. The value is the number of 15-minute increments from UTC. Positive numbers are eastwards.
     */
    public MeshTAITime(Integer taiSeconds, byte subSecond, byte uncertainty, boolean timeAuthority, short utcDelta, byte timeZoneOffset) {
        this.taiSeconds = taiSeconds;
        this.subSecond = subSecond;
        this.uncertainty = uncertainty;
        this.timeAuthority = timeAuthority;
        this.utcDelta = utcDelta;
        this.timeZoneOffset = timeZoneOffset;
    }

    public Integer getTaiSeconds() {
        return taiSeconds;
    }

    public byte getSubSecond() {
        return subSecond;
    }

    public byte getUncertainty() {
        return uncertainty;
    }

    public boolean isTimeAuthority() {
        return timeAuthority;
    }

    public short getUtcDelta() {
        return utcDelta;
    }

    public byte getTimeZoneOffset() {
        return timeZoneOffset;
    }
}
