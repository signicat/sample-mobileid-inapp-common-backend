package com.signicat.demo.sampleapp.inapp.web.controllers;

import java.util.ArrayList;
import java.util.List;

import com.signicat.demo.sampleapp.inapp.common.exception.ApplicationException;
import com.signicat.demo.sampleapp.inapp.common.wsclient.ScidWsClient;
import com.signicat.generated.scid.Device;
import com.signicat.generated.scid.Devices;
import com.signicat.generated.scid.GetDevicesResponse;

public class ControllersUtil {

    private ControllersUtil() {}

    public static Devices getAllDevices(final ScidWsClient scidWsClient,final String extRef) {
        if (!scidWsClient.accountExists(extRef)) {
            throw new ApplicationException("Account " + extRef + " does not exist");
        }

        final GetDevicesResponse result = (GetDevicesResponse) scidWsClient.getDevices(extRef);
        return result.getDevices();
    }


    public static String getDeviceId(final Devices devices, final String deviceName) {
        // --- Filter to extract deviceID for the one with deviceName
        for (final Device device : devices.getDevice()) {
            if (device.getName().equalsIgnoreCase(deviceName)) {
                return device.getId();
            }
        }
        throw new ApplicationException("No device " + deviceName + " found for specified account ");
    }

    public static List<String> getListOfDeviceNames(final Devices devices) {
        final List<String> deviceNames = new ArrayList<>();
        for (final Device device : devices.getDevice()) {
            if (device.getType() != null && device.getType().equals("MOBILEID")) {
                deviceNames.add(device.getName());
            }
        }
        return deviceNames;
    }
}
