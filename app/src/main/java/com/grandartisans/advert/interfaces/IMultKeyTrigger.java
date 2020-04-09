package com.grandartisans.advert.interfaces;
/**
 * 组合按键触发功能的接口
 *
 */
public interface  IMultKeyTrigger {
	 /**
     * 是否允许触发，也就是触发组合键的条件
     * @return
     */
    boolean allowTrigger();
    /**
     * 检查输入的按键是否是对应组合键某个位置
     * @param keycode
     * @param eventTime
     * @return
     */
    boolean checkKey(int keycode, long eventTime);
    /**
     * 检查组合键是否已经输入完成
     * @return
     */
    boolean checkMultKey();
    /**
     * 清除所有记录的键
     */
    void clearKeys();
    /**
     * 组合键触发事件
     */
    void onTrigger();
}
