package top.jie65535.jhr.game

/**
 * 赌注
 *
 * @param id 下注用户
 * @param number 马号
 * @param score 下注点数
 */
data class Bet(val id: Long, val number: Int, val score: Int)