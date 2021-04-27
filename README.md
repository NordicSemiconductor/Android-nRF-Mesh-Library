# Android-nRF-Mesh-Library
[ ![Download](https://api.bintray.com/packages/nordic/android/no.nordicsemi.android%3Amesh/images/download.svg) ](https://bintray.com/nordic/android/no.nordicsemi.android%3Amesh/_latestVersion)

## About
The Android-nRF-Mesh-Library allows provisioning and sending messages to Bluetooth Mesh devices. 

> Bluetooth Mesh specification may be found here: https://www.bluetooth.com/specifications/mesh-specifications/

The library is compatible with version 1.0.1 of the Bluetooth Mesh Profile Specification.

nRF Mesh for Android is supported on Android devices running Android 4.3 and onwards.

### Features
1. Provisionig with all features that available in Bluetooth Mesh Profile 1.0.1, including OOB Public Key and all types of OOB.
2. Managing Provisioners, Network Keys, Application Keys, resetting network, etc.
3. All network layers are working.
4. Parsing Secure Network beacons.
5. Adding, removing and refreshing Network and Application Keys to Nodes.
6. Binding and unbinding Application Keys to Models.
7. Setting and clearing publication to a Model.
8. Setting and removing subscriptions to a Model.
9. Groups, including those with Virtual Addresses.
10. UI for controlling groups (Generic OnOff and Generic Level (delta) are supported).
12. Handling Configuration Server message sent by other Provisioner.
13. Generic OnOff and Vendor model have dedicated controls in sample app.
14. Proxy Filter.
15. IV Index update (handling updates received in Secure Network beacons).


## Requirements

* Android Studio
* An Android device with BLE capabilities

## Optional

* nrf52832/840 based Development Kit(s) to test the sample firmwares on.

## Installation

* Open 'Example/nRf Mesh Provisioner'
* Connect an Android device.
* Build and run project.
* To be able to quickly start testing, use the bundled firmwares directory named `ExampleFirmwares` that includes a light server (Light source) and a light client (Switch) firmwares. those firmwares will work on a `nrf52832` DevKit.

## How to include it in your own project

#### Maven Central

The library may be found on the Maven Central repository. 
Add it to your project by adding the following dependency:

```grovy
implementation 'no.nordicsemi.android:mesh:2.4.3'
```

#### Manual

Clone this project and add *ble* module as a dependency to your project:

1. In *settings.gradle* file add the following lines:
```groovy
include ':mesh'
project(':mesh').projectDir = file('../Android-Mesh-Library/mesh')
```

2. In *app/build.gradle* file add `implementation project(':mesh')` inside dependencies.
3. Sync project and build it.

See example projects in this repository.

#### Sample

To start using the library in your own project take a look at the following snippet.
```java
        MeshManagerApi mMeshManagerApi = new MeshManagerApi(context);
        mMeshManagerApi.setMeshManagerCallbacks(this);
        mMeshManagerApi.setProvisioningStatusCallbacks(this);
        mMeshManagerApi.setMeshStatusCallbacks(this);
        mMeshManagerApi.loadMeshNetwork();
```

The sample application uses the [Android BLE Library](https://github.com/NordicSemiconductor/Android-BLE-Library/) by Nordic Semiconductor ASA and is recommended to use this dependency in your application. Follow the snippet below when using the Android-Ble-Library in combination with the Android-Mesh-Library to send and receive data.
```java
    @Override
    public void onDataReceived(final BluetoothDevice bluetoothDevice, final int mtu, final byte[] pdu) {
        mMeshManagerApi.handleNotifications(mtu, pdu);
    }

    @Override
    public void onDataSent(final BluetoothDevice device, final int mtu, final byte[] pdu) {
        mMeshManagerApi.handleWriteCallbacks(mtu, pdu);
    }
```
When using your own ble library/module call the `mMeshManagerApi.handleNotifications(mtu, pdu);` and `mMeshManagerApi.handleWriteCallbacks(mtu, pdu);` to send and receive data.

Provisioning a node in to the network can be done in three steps,

1.	Connect to the node advertising with the Mesh Provisioning UUID
2.	Identify the node to be provisioned, where the devices will blink, vibrate or beep depending on the capabilities for pre-defined duration of 5 seconds. This is useful when provisioning multiple nodes. To identify a node call
```java
    void identifyNode(@NonNull final UUID deviceUUID) throws IllegalArgumentException; 
```
by passing the device uuid of the unprovisioned mesh node.
or call
```java
    void identifyNode(@NonNull final UUID deviceUUID, final int attentionTimer) throws IllegalArgumentException;
```
by passing the device uuid of the unprovisioned mesh node and the desired duration.
3.  Depending on the identified device capabilities, call one of the following functions to provision the node.
```java
    void startProvisioning(@NonNull final UnprovisionedMeshNode unprovisionedMeshNode) throws IllegalArgumentException;
```
or
```java
    void startProvisioningWithStaticOOB(@NonNull final UnprovisionedMeshNode unprovisionedMeshNode) throws IllegalArgumentException;
```
or
```java
    void startProvisioningWithOutputOOB(@NonNull final UnprovisionedMeshNode unprovisionedMeshNode, final OutputOOBAction oobAction) throws IllegalArgumentException;
```
or
```java
    void startProvisioningWithInputOOB(@NonNull final UnprovisionedMeshNode unprovisionedMeshNode, @NonNull final InputOOBAction oobAction) throws IllegalArgumentException;
```
Use the `MeshNetowrk` object to edit Mesh Network properties such as Network name, Provisioners and their properties (Name Address, TTL and Address Ranges), Network Keys, App Keys.

Following is an example on how to send a GenericOnOffSet message.
```java
final GenericOnOffSet genericOnOffSet = new GenericOnOffSet(appKey, state,
                                new Random().nextInt(), transitionSteps, transitionStepResolution, delay);
mMeshManagerAPi.createMeshPdu(address, genericOnOffSet);
```
and Config messages can also be sent similarly. 

## Author

Mobile Applications Team, Nordic Semiconductor ASA.

Contact: roshanrajaratnam <roshan.rajaratnam@nordicsemi.no>

## License

The Android-nRF-Mesh-Library is available under BSD 3-Clause license. See the LICENSE file for more info.
