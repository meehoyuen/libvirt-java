package org.libvirt;

import org.libvirt.jna.DomainPointer;
import org.libvirt.jna.Libvirt;
import org.libvirt.jna.virDomainBlockStats;
import org.libvirt.jna.virDomainInfo;
import org.libvirt.jna.virDomainInterfaceStats;
import org.libvirt.jna.virSchedParameter;
import org.libvirt.jna.virVcpuInfo;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.ptr.IntByReference;

/**
 * A virtual machine defined within libvirt.
 */
public class Domain {

    static final class CreateFlags {
        static final int VIR_DOMAIN_NONE = 0;
    }

    static final class MigrateFlags {
        /**
         * live migration
         */
        static final int VIR_MIGRATE_LIVE = 1;
    }

    static final class XMLFlags {
        /**
         * dump security sensitive information too
         */
        static final int VIR_DOMAIN_XML_SECURE = 1;
        /**
         * dump inactive domain information
         */
        static final int VIR_DOMAIN_XML_INACTIVE = 2;
    }

    /**
     * the native virDomainPtr.
     */
    private DomainPointer VDP;

    /**
     * The Connect Object that represents the Hypervisor of this Domain
     */
    private Connect virConnect;

    /**
     * The libvirt connection from the hypervisor
     */
    protected Libvirt libvirt;

    /**
     * Constructs a Domain object from a known native virDomainPtr, and a
     * Connect object. For use when native libvirt returns a virConnectPtr, i.e.
     * error handling.
     * 
     * @param virConnect
     *            the Domain's hypervisor
     * @param VDP
     *            the native virDomainPtr
     */
    Domain(Connect virConnect, DomainPointer VDP) {
        this.virConnect = virConnect;
        this.VDP = VDP;
        this.libvirt = virConnect.libvirt;
    }

    /**
     * Creates a virtual device attachment to backend.
     * 
     * @param xmlDesc
     *            XML description of one device
     * @throws LibvirtException
     */
    public void attachDevice(String xmlDesc) throws LibvirtException {
        libvirt.virDomainAttachDevice(VDP, xmlDesc);
        processError();
    }

    /**
     * Returns block device (disk) stats for block devices attached to this
     * domain. The path parameter is the name of the block device. Get this by
     * calling virDomainGetXMLDesc and finding the <target dev='...'> attribute
     * within //domain/devices/disk. (For example, "xvda"). Domains may have
     * more than one block device. To get stats for each you should make
     * multiple calls to this function. Individual fields within the
     * DomainBlockStats object may be returned as -1, which indicates that the
     * hypervisor does not support that particular statistic.
     * 
     * @param path
     *            path to the block device
     * @return the statistics in a DomainBlockStats object
     * @throws LibvirtException
     */
    public DomainBlockStats blockStats(String path) throws LibvirtException {
        virDomainBlockStats stats = new virDomainBlockStats();
        libvirt.virDomainBlockStats(VDP, path, stats, stats.size());
        processError();
        return new DomainBlockStats(stats);
    }

    /**
     * Dumps the core of this domain on a given file for analysis. Note that for
     * remote Xen Daemon the file path will be interpreted in the remote host.
     * 
     * @param to
     *            path for the core file
     * @param flags
     *            extra flags, currently unused
     * @throws LibvirtException
     */
    public void coreDump(String to, int flags) throws LibvirtException {
        libvirt.virDomainCoreDump(VDP, to, flags);
        processError();
    }

    /**
     * It returns the length (in bytes) required to store the complete CPU map
     * between a single virtual & all physical CPUs of a domain.
     * 
     */
    public int cpuMapLength(int maxCpus) {
        return (((maxCpus) + 7) / 8);
    }

    /**
     * Launches this defined domain. If the call succeed the domain moves from
     * the defined to the running domains pools.
     * 
     * @throws LibvirtException
     */
    public void create() throws LibvirtException {
        libvirt.virDomainCreate(VDP);
        processError();
    }

    /**
     * Destroys this domain object. The running instance is shutdown if not down
     * already and all resources used by it are given back to the hypervisor.
     * The data structure is freed and should not be used thereafter if the call
     * does not return an error. This function may requires priviledged access
     * 
     * @throws LibvirtException
     */
    public void destroy() throws LibvirtException {
        libvirt.virDomainDestroy(VDP);
        processError();
    }

    /**
     * Destroys a virtual device attachment to backend.
     * 
     * @param xmlDesc
     *            XML description of one device
     * @throws LibvirtException
     */
    public void detachDevice(String xmlDesc) throws LibvirtException {
        libvirt.virDomainDetachDevice(VDP, xmlDesc);
        processError();
    }

    /**
     * Frees this domain object. The running instance is kept alive. The data
     * structure is freed and should not be used thereafter.
     * 
     * @throws LibvirtException
     * @returns 0 for success, -1 for failure.
     */
    public int free() throws LibvirtException {
        int success = 0 ;
        if (VDP != null) {
            success = libvirt.virDomainFree(VDP);
            processError();
            VDP = null;
        }

        return success ;
    }

    /**
     * Provides a boolean value indicating whether the network is configured to
     * be automatically started when the host machine boots.
     * 
     * @return the result
     * @throws LibvirtException
     */
    public boolean getAutostart() throws LibvirtException {
        IntByReference autoStart = new IntByReference();
        libvirt.virDomainGetAutostart(VDP, autoStart);
        processError();
        return autoStart.getValue() != 0 ? true : false;
    }

    /**
     * Provides the connection object associated with a domain.
     * 
     * @return the Connect object
     */
    public Connect getConnect() {
        return virConnect;
    }

    /**
     * Gets the hypervisor ID number for the domain
     * 
     * @return the hypervisor ID
     * @throws LibvirtException
     */
    public int getID() throws LibvirtException {
        int returnValue = libvirt.virDomainGetID(VDP);
        processError();
        return returnValue;
    }

    /**
     * Extract information about a domain. Note that if the connection used to
     * get the domain is limited only a partial set of the information can be
     * extracted.
     * 
     * @return a DomainInfo object describing this domain
     * @throws LibvirtException
     */
    public DomainInfo getInfo() throws LibvirtException {
        DomainInfo returnValue = null;
        virDomainInfo vInfo = new virDomainInfo();
        int success = libvirt.virDomainGetInfo(VDP, vInfo);
        processError();
        if (success == 0) {
            returnValue = new DomainInfo(vInfo);
        }
        return returnValue;
    }

    /**
     * Retrieve the maximum amount of physical memory allocated to a domain.
     * 
     * @return the memory in kilobytes
     * @throws LibvirtException
     */
    public long getMaxMemory() throws LibvirtException {
        NativeLong returnValue = libvirt.virDomainGetMaxMemory(VDP);
        processError();
        return returnValue.longValue();
    }

    /**
     * Provides the maximum number of virtual CPUs supported for the guest VM.
     * If the guest is inactive, this is basically the same as
     * virConnectGetMaxVcpus. If the guest is running this will reflect the
     * maximum number of virtual CPUs the guest was booted with.
     * 
     * @return the number of VCPUs
     * @throws LibvirtException
     */
    public int getMaxVcpus() throws LibvirtException {
        int returnValue = libvirt.virDomainGetMaxVcpus(VDP);
        processError();
        return returnValue;
    }

    /**
     * Gets the public name for this domain
     * 
     * @return the name
     * @throws LibvirtException
     */
    public String getName() throws LibvirtException {
        String returnValue = libvirt.virDomainGetName(VDP);
        processError();
        return returnValue;
    }

    /**
     * Gets the type of domain operation system.
     * 
     * @return the type
     * @throws LibvirtException
     */
    public String getOSType() throws LibvirtException {
        String returnValue = libvirt.virDomainGetOSType(VDP);
        processError();
        return returnValue;
    }

    /**
     * Gets the scheduler parameters.
     * 
     * @return an array of SchedParameter objects
     * @throws LibvirtException
     */
    public SchedParameter[] getSchedulerParameters() throws LibvirtException {
        IntByReference nParams = new IntByReference();
        SchedParameter[] returnValue = new SchedParameter[0];
        String scheduler = libvirt.virDomainGetSchedulerType(VDP, nParams);
        processError();
        if (scheduler != null) {
            virSchedParameter[] nativeParams = new virSchedParameter[nParams.getValue()];
            returnValue = new SchedParameter[nParams.getValue()];
            libvirt.virDomainGetSchedulerParameters(VDP, nativeParams, nParams);
            processError();
            for (int x = 0; x < nParams.getValue(); x++) {
                returnValue[x] = SchedParameter.create(nativeParams[x]);
            }
        }

        return returnValue;
    }

    // getSchedulerType
    // We don't expose the nparams return value, it's only needed for the
    // SchedulerParameters allocations,
    // but we handle that in getSchedulerParameters internally.
    /**
     * Gets the scheduler type.
     * 
     * @return the type of the scheduler
     * @throws LibvirtException
     */
    public String[] getSchedulerType() throws LibvirtException {
        IntByReference nParams = new IntByReference();
        String returnValue = libvirt.virDomainGetSchedulerType(VDP, nParams);
        processError();
        String[] array = new String[1];
        array[0] = returnValue;
        return array;
    }

    /**
     * Get the UUID for this domain.
     * 
     * @return the UUID as an unpacked int array
     * @throws LibvirtException
     * @see <a href="http://www.ietf.org/rfc/rfc4122.txt">rfc4122</a>
     */
    public int[] getUUID() throws LibvirtException {
        byte[] bytes = new byte[Libvirt.VIR_UUID_BUFLEN];
        int success = libvirt.virDomainGetUUID(VDP, bytes);
        processError();
        int[] returnValue = new int[0];
        if (success == 0) {
            returnValue = Connect.convertUUIDBytes(bytes);
        }
        return returnValue;
    }

    /**
     * Gets the UUID for this domain as string.
     * 
     * @return the UUID in canonical String format
     * @throws LibvirtException
     * @see <a href="http://www.ietf.org/rfc/rfc4122.txt">rfc4122</a>
     */
    public String getUUIDString() throws LibvirtException {
        byte[] bytes = new byte[Libvirt.VIR_UUID_STRING_BUFLEN];
        int success = libvirt.virDomainGetUUIDString(VDP, bytes);
        processError();
        String returnValue = null;
        if (success == 0) {
            returnValue = Native.toString(bytes);
        }
        return returnValue;
    }

    /**
     * Returns the cpumaps for this domain Only the lower 8 bits of each int in
     * the array contain information.
     * 
     * @return a bitmap of real CPUs for all vcpus of this domain
     * @throws LibvirtException
     */
    public int[] getVcpusCpuMaps() throws LibvirtException {
        int[] returnValue = new int[0];
        int cpuCount = this.getMaxVcpus();

        if (cpuCount > 0) {
            NodeInfo nodeInfo = virConnect.nodeInfo();
            int maplength = cpuMapLength(nodeInfo.maxCpus());
            virVcpuInfo[] infos = new virVcpuInfo[cpuCount];
            returnValue = new int[cpuCount * maplength];
            byte[] cpumaps = new byte[cpuCount * maplength];
            libvirt.virDomainGetVcpus(VDP, infos, cpuCount, cpumaps, maplength);
            processError();
            for (int x = 0; x < cpuCount * maplength; x++) {
                returnValue[x] = cpumaps[x];
            }
        }
        return returnValue;
    }

    /**
     * Extracts information about virtual CPUs of this domain
     * 
     * @return an array of VcpuInfo object describing the VCPUs
     * @throws LibvirtException
     */
    public VcpuInfo[] getVcpusInfo() throws LibvirtException {
        int cpuCount = this.getMaxVcpus();
        VcpuInfo[] returnValue = new VcpuInfo[cpuCount];
        virVcpuInfo[] infos = new virVcpuInfo[cpuCount];
        libvirt.virDomainGetVcpus(VDP, infos, cpuCount, null, 0);
        processError();
        for (int x = 0; x < cpuCount; x++) {
            returnValue[x] = new VcpuInfo(infos[x]);
        }
        return returnValue;
    }

    /**
     * Provides an XML description of the domain. The description may be reused
     * later to relaunch the domain with createLinux().
     * 
     * @param flags
     *            not used
     * @return the XML description String
     * @throws LibvirtException
     * @see <a href="http://libvirt.org/format.html#Normal1" >The XML
     *      Description format </a>
     */
    public String getXMLDesc(int flags) throws LibvirtException {
        String returnValue = libvirt.virDomainGetXMLDesc(VDP, flags);
        processError();
        return returnValue;
    }

    /**
     * Returns network interface stats for interfaces attached to this domain.
     * The path parameter is the name of the network interface. Domains may have
     * more than network interface. To get stats for each you should make
     * multiple calls to this function. Individual fields within the
     * DomainInterfaceStats object may be returned as -1, which indicates that
     * the hypervisor does not support that particular statistic.
     * 
     * @param path
     *            path to the interface
     * @return the statistics in a DomainInterfaceStats object
     * @throws LibvirtException
     */
    public DomainInterfaceStats interfaceStats(String path) throws LibvirtException {
        virDomainInterfaceStats stats = new virDomainInterfaceStats();
        libvirt.virDomainInterfaceStats(VDP, path, stats, stats.size());
        processError();
        return new DomainInterfaceStats(stats);
    }

    /**
     * Migrate this domain object from its current host to the destination host
     * given by dconn (a connection to the destination host). Flags may be one
     * of more of the following: Domain.VIR_MIGRATE_LIVE Attempt a live
     * migration. If a hypervisor supports renaming domains during migration,
     * then you may set the dname parameter to the new name (otherwise it keeps
     * the same name). If this is not supported by the hypervisor, dname must be
     * NULL or else you will get an error. Since typically the two hypervisors
     * connect directly to each other in order to perform the migration, you may
     * need to specify a path from the source to the destination. This is the
     * purpose of the uri parameter. If uri is NULL, then libvirt will try to
     * find the best method. Uri may specify the hostname or IP address of the
     * destination host as seen from the source. Or uri may be a URI giving
     * transport, hostname, user, port, etc. in the usual form. Refer to driver
     * documentation for the particular URIs supported. The maximum bandwidth
     * (in Mbps) that will be used to do migration can be specified with the
     * bandwidth parameter. If set to 0, libvirt will choose a suitable default.
     * Some hypervisors do not support this feature and will return an error if
     * bandwidth is not 0. To see which features are supported by the current
     * hypervisor, see Connect.getCapabilities,
     * /capabilities/host/migration_features. There are many limitations on
     * migration imposed by the underlying technology - for example it may not
     * be possible to migrate between different processors even with the same
     * architecture, or between different types of hypervisor.
     * 
     * @param dconn
     *            destination host (a Connect object)
     * @param flags
     *            flags
     * @param dname
     *            (optional) rename domain to this at destination
     * @param uri
     *            (optional) dest hostname/URI as seen from the source host
     * @param bandwidth
     *            optional) specify migration bandwidth limit in Mbps
     * @return the new domain object if the migration was successful, or NULL in
     *         case of error. Note that the new domain object exists in the
     *         scope of the destination connection (dconn).
     * @throws LibvirtException
     */
    public Domain migrate(Connect dconn, long flags, String dname, String uri, long bandwidth) throws LibvirtException {
        DomainPointer newPtr = libvirt.virDomainMigrate(VDP, dconn.VCP, new NativeLong(flags), dname, uri, new NativeLong(bandwidth));
        processError();
        return new Domain(dconn, newPtr);
    }

    /**
     * Dynamically changes the real CPUs which can be allocated to a virtual
     * CPU. This function requires priviledged access to the hypervisor.
     * 
     * @param vcpu
     *            virtual cpu number
     * @param cpumap
     *            bit map of real CPUs represented by the the lower 8 bits of
     *            each int in the array. Each bit set to 1 means that
     *            corresponding CPU is usable. Bytes are stored in little-endian
     *            order: CPU0-7, 8-15... In each byte, lowest CPU number is
     *            least significant bit.
     * @throws LibvirtException
     */
    public void pinVcpu(int vcpu, int[] cpumap) throws LibvirtException {
        byte[] packedMap = new byte[cpumap.length];
        for (int x = 0; x < cpumap.length; x++) {
            packedMap[x] = (byte) cpumap[x];
        }
        libvirt.virDomainPinVcpu(VDP, vcpu, packedMap, cpumap.length);
        processError();
    }

    /**
     * Error handling logic to throw errors. Must be called after every libvirt
     * call.
     */
    protected void processError() throws LibvirtException {
        virConnect.processError();
    }

    /**
     * Reboot this domain, the domain object is still usable there after but the
     * domain OS is being stopped for a restart. Note that the guest OS may
     * ignore the request.
     * 
     * @param flags
     *            extra flags for the reboot operation, not used yet
     * @throws LibvirtException
     */
    public void reboot(int flags) throws LibvirtException {
        libvirt.virDomainReboot(VDP, flags);
        processError();
    }

    /**
     * Resume this suspended domain, the process is restarted from the state
     * where it was frozen by calling virSuspendDomain(). This function may
     * requires privileged access
     * 
     * @throws LibvirtException
     */
    public void resume() throws LibvirtException {
        libvirt.virDomainResume(VDP);
        processError();
    }

    /**
     * Suspends this domain and saves its memory contents to a file on disk.
     * After the call, if successful, the domain is not listed as running
     * anymore (this may be a problem). Use Connect.virDomainRestore() to
     * restore a domain after saving.
     * 
     * @param to
     *            path for the output file
     * @throws LibvirtException
     */
    public void save(String to) throws LibvirtException {
        libvirt.virDomainSave(VDP, to);
        processError();
    }

    /**
     * Configures the network to be automatically started when the host machine
     * boots.
     * 
     * @param autostart
     * @throws LibvirtException
     */
    public void setAutostart(boolean autostart) throws LibvirtException {
        int autoValue = autostart ? 1 : 0;
        libvirt.virDomainSetAutostart(VDP, autoValue);
        processError();
    }

    /**
     * * Dynamically change the maximum amount of physical memory allocated to a
     * domain. This function requires priviledged access to the hypervisor.
     * 
     * @param memory
     *            the amount memory in kilobytes
     * @throws LibvirtException
     */
    public void setMaxMemory(long memory) throws LibvirtException {
        libvirt.virDomainSetMaxMemory(VDP, new NativeLong(memory));
        processError();
    }

    /**
     * Dynamically changes the target amount of physical memory allocated to
     * this domain. This function may requires priviledged access to the
     * hypervisor.
     * 
     * @param memory
     *            in kilobytes
     * @throws LibvirtException
     */
    public void setMemory(long memory) throws LibvirtException {
        libvirt.virDomainSetMemory(VDP, new NativeLong(memory));
        processError();
    }

    /**
     * Changes the scheduler parameters
     * 
     * @param params
     *            an array of SchedParameter objects to be changed
     * @throws LibvirtException
     */
    public void setSchedulerParameters(SchedParameter[] params) throws LibvirtException {
        IntByReference nParams = new IntByReference();
        nParams.setValue(params.length);
        virSchedParameter[] input = new virSchedParameter[params.length];
        for (int x = 0; x < params.length; x++) {
            input[x] = SchedParameter.toNative(params[x]);
        }
        libvirt.virDomainSetSchedulerParameters(VDP, input, nParams);
        processError();
    }

    /**
     * Dynamically changes the number of virtual CPUs used by this domain. Note
     * that this call may fail if the underlying virtualization hypervisor does
     * not support it or if growing the number is arbitrary limited. This
     * function requires priviledged access to the hypervisor.
     * 
     * @param nvcpus
     *            the new number of virtual CPUs for this domain
     * @throws LibvirtException
     */
    public void setVcpus(int nvcpus) throws LibvirtException {
        libvirt.virDomainSetVcpus(VDP, nvcpus);
        processError();
    }

    /**
     * Shuts down this domain, the domain object is still usable there after but
     * the domain OS is being stopped. Note that the guest OS may ignore the
     * request. TODO: should we add an option for reboot, knowing it may not be
     * doable in the general case ?
     * 
     * @throws LibvirtException
     */
    public void shutdown() throws LibvirtException {
        libvirt.virDomainShutdown(VDP);
        processError();
    }

    /**
     * Suspends this active domain, the process is frozen without further access
     * to CPU resources and I/O but the memory used by the domain at the
     * hypervisor level will stay allocated. Use Domain.resume() to reactivate
     * the domain. This function requires priviledged access.
     * 
     * @throws LibvirtException
     */
    public void suspend() throws LibvirtException {
        libvirt.virDomainSuspend(VDP);
        processError();
    }

    /**
     * undefines this domain but does not stop it if it is running
     * 
     * @throws LibvirtException
     */
    public void undefine() throws LibvirtException {
        libvirt.virDomainUndefine(VDP);
        processError();
    }

}