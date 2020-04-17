package com.tangmj.distributed.commons.redisson;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author tangmingjian 2020-04-16 下午4:50
 **/
@ConfigurationProperties(prefix = "redisson")
@Data
public class RedissonProperties {

    private static final String PREFIX = "redis://";

    private int timeout = 3000;

    private String password;

    private int database = 0;

    private int connectionPoolSize = 64;

    private int connectionMinimumIdleSize = 24;

    private int slaveConnectionPoolSize = 64;

    private int masterConnectionPoolSize = 64;


    /***************************** cluster config *****************************/

    private String address;

    public void setAddress(String address) {
        if (!StringUtils.isEmpty(address)) {
            this.address = PREFIX.concat(address);
        }
    }

    /***************************** sentinel config *****************************/

    private String masterName;

    private String[] sentinelAddresses;

    public void setSentinelAddresses(String[] sentinelAddresses) {
        if (sentinelAddresses != null && sentinelAddresses.length > 0) {
            this.sentinelAddresses = Arrays.stream(sentinelAddresses)
                    .map(o -> PREFIX.concat(o))
                    .collect(Collectors.toList())
                    .toArray(new String[]{});
        }
    }

    /***************************** cluster config *****************************/
    /**
     * Default value: 1000
     * Redis cluster scan interval in milliseconds.
     */
    private int scaninterval = 1000;

    /**
     * default value SLAVE
     * Set node type used for read operation. Available values:
     * SLAVE - Read from slave nodes, uses MASTER if no SLAVES are available.
     * MASTER - Read from master node,
     * MASTER_SLAVE - Read from master and slave nodes
     */
    private String readMode = "SLAVE";

    /**
     * Default value: SLAVE
     * Set node type used for subscription operation. Available values:
     * SLAVE - Subscribe to slave nodes,
     * MASTER - Subscribe to master node,
     */
    private String subscriptionMode = "SLAVE";

    private String[] nodeAddresses;

    public void setNodeAddresses(String[] nodeAddresses) {
        if (nodeAddresses != null && nodeAddresses.length > 0) {
            this.nodeAddresses = Arrays.stream(nodeAddresses)
                    .map(o -> PREFIX.concat(o))
                    .collect(Collectors.toList())
                    .toArray(new String[]{});
        }
    }
}
