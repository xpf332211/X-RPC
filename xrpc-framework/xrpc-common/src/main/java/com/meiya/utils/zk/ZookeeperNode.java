package com.meiya.utils.zk;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author xiaopf
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ZookeeperNode {
    private String nodePath;
    private byte[] data;
}
