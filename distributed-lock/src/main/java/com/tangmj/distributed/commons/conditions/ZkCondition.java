package com.tangmj.distributed.commons.conditions;

/**
 * @author tangmingjian 2019-12-19 下午2:33
 **/
public class ZkCondition extends BaseCondition {
    @Override
    protected String lockType() {
        return "zookeeper";
    }
}
