package com.moodfood.service;

import com.moodfood.domain.Mood;
import com.moodfood.domain.DesiredTaste;

/**
 * Template-based recommendation explanations (spec §14: "처음에는 템플릿
 * 방식으로 구현한다. LLM을 핵심 추천 로직으로 사용하지 않는다.").
 */
public class RecommendationReasonGenerator {

  private RecommendationReasonGenerator() {}

  public static String generate(UserContext context, String recipeTitle) {
    String moodPhrase = moodPhrase(context.mood());
    String tastePhrase = tastePhrase(context.desiredTaste());
    return "오늘처럼 " + moodPhrase + " 날, " + tastePhrase + " 원한다는 선택과 잘 맞는 메뉴예요.";
  }

  private static String moodPhrase(Mood mood) {
    return switch (mood) {
      case GOOD -> "기분 좋은";
      case NEUTRAL -> "그냥 그런";
      case TIRED -> "피곤한";
      case STRESSED -> "스트레스가 있는";
      case SAD -> "우울한";
      case EXCITED -> "신나는";
      case DRAINED -> "아무것도 하기 싫은";
    };
  }

  private static String tastePhrase(DesiredTaste taste) {
    return switch (taste) {
      case SPICY -> "매콤한 음식을";
      case WARM -> "따뜻한 음식을";
      case HEARTY -> "든든한 한 끼를";
      case FAMILIAR -> "익숙한 음식을";
      case LIGHT -> "가벼운 음식을";
      case SWEET -> "달달한 음식을";
      case SPECIAL -> "특별한 한 끼를";
      case SURPRISE_ME -> "새로운 걸";
    };
  }
}
