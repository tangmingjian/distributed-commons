package com.tangmj.distributed.commons;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author tangmingjian 2020-03-28 下午6:10
 **/
@Data
@AllArgsConstructor
public class MLock<Lock> {
    private Lock lock;
}
