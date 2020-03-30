package com.tangmj.distributed.commons.conditions;

/**
 * @author tangmingjian 2020-03-30 上午10:38
 **/
public class LocalCondition extends BaseCondition {
    @Override
    protected String lockType() {
        return "local";
    }
}
