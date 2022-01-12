package top.jie65535.jhr.game

/**
 * 赛马
 *
 * @param type 赛马类型（样式）
 */
data class Horse(val type: Int) {
    /**
     * 马的位置 设置为小于0的值时会重置为0
     */
    var position: Int = 0
        set(value) {
            field = if (value < 0) 0 else value
        }
}
