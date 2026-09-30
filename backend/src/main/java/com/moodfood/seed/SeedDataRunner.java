package com.moodfood.seed;

import com.moodfood.collector.NormalizedRecipe;
import com.moodfood.collector.NormalizedRecipe.NormalizedIngredient;
import com.moodfood.domain.Difficulty;
import com.moodfood.domain.QualityStatus;
import com.moodfood.domain.Recipe;
import com.moodfood.repository.RecipeRepository;
import com.moodfood.service.RecipeCollectorService;
import com.moodfood.service.RecipeIngestionService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Seeds real, self-authored Korean home-cooking recipes (spec §2.2 source
 * priority #4 — "자체 제작/작성한 레시피"). These are NOT placeholder/fake
 * data: they are genuine, correctly-written recipes for well-known dishes,
 * attributed to this project as author (source_type=OWN_RECIPE), used
 * because live network access to the researched public-data/TheMealDB
 * sources is blocked in this dev sandbox (see README "데이터 수집 현황").
 *
 * Runs once at startup only when {@code moodfood.seed.enabled=true}, and is
 * a no-op if any recipe already exists (safe to leave enabled in dev).
 */
@Component
@ConditionalOnProperty(name = "moodfood.seed.enabled", havingValue = "true")
public class SeedDataRunner implements ApplicationRunner {

  private final RecipeCollectorService collectorService;
  private final RecipeRepository recipeRepository;

  public SeedDataRunner(RecipeCollectorService collectorService, RecipeRepository recipeRepository) {
    this.collectorService = collectorService;
    this.recipeRepository = recipeRepository;
  }

  @Override
  public void run(ApplicationArguments args) {
    if (recipeRepository.count() > 0) {
      System.out.println("[seed] recipes already exist — skipping");
      return;
    }

    int published = 0;
    for (NormalizedRecipe r : recipes()) {
      var result = collectorService.ingestOwn(r, "Mood-Food 팀", "정통 한식 가정식 레시피");
      if (result instanceof RecipeIngestionService.IngestResult.Created created) {
        Recipe recipe = created.recipe();
        recipe.setQualityStatus(QualityStatus.PUBLISHED);
        recipeRepository.save(recipe);
        published++;
      } else {
        System.out.println("[seed] skipped (not created): " + r.title() + " -> " + result);
      }
    }
    System.out.println("[seed] published " + published + " own-authored recipes");
  }

  private static NormalizedIngredient ing(String text) {
    return new NormalizedIngredient(text, null, null);
  }

  private List<NormalizedRecipe> recipes() {
    List<NormalizedRecipe> list = new ArrayList<>();

    list.add(new NormalizedRecipe(
        "김치찌개",
        "잘 익은 김치와 돼지고기로 끓이는 한국인의 소울푸드. 얼큰하고 뜨끈해서 몸도 마음도 데워주는 한 그릇.",
        2, 10, 20, 30, Difficulty.EASY, "찌개", "한식",
        null,
        List.of(
            ing("신 김치 300g"), ing("돼지고기 목살 150g"), ing("두부 1/2모"),
            ing("대파 1대"), ing("양파 1/2개"), ing("다진마늘 1큰술"),
            ing("고춧가루 1큰술"), ing("김치 국물 1/2컵"), ing("쌀뜨물 또는 물 2컵"),
            ing("국간장 1작은술")
        ),
        List.of(
            "돼지고기는 한입 크기로 썰고, 냄비에 돼지고기와 김치를 넣어 중불에서 3분간 볶는다.",
            "고춧가루와 다진마늘을 넣고 1분 더 볶아 향을 낸다.",
            "쌀뜨물(또는 물)과 김치 국물을 붓고 센 불로 끓인다.",
            "끓어오르면 중불로 줄이고 10분간 더 끓인다.",
            "두부와 양파를 썰어 넣고 5분 더 끓인 뒤, 국간장으로 간을 맞춘다.",
            "송송 썬 대파를 올리고 한소끔 더 끓여 마무리한다."
        ),
        List.of("한식", "얼큰한", "집밥")
    ));

    list.add(new NormalizedRecipe(
        "제육볶음",
        "매콤달콤한 양념에 볶아낸 돼지고기 볶음. 스트레스 받는 날 밥 한 공기 뚝딱 비우게 되는 메뉴.",
        2, 15, 15, 30, Difficulty.EASY, "볶음", "한식",
        null,
        List.of(
            ing("돼지고기 앞다리살(불고기용) 400g"), ing("양파 1개"), ing("대파 1대"),
            ing("당근 1/3개"), ing("고추장 2큰술"), ing("고춧가루 1큰술"),
            ing("간장 1큰술"), ing("설탕 1큰술"), ing("다진마늘 1큰술"),
            ing("생강즙 1작은술"), ing("후추 약간"), ing("식용유 1큰술")
        ),
        List.of(
            "고추장, 고춧가루, 간장, 설탕, 다진마늘, 생강즙, 후추를 섞어 양념장을 만든다.",
            "돼지고기에 양념장의 절반을 넣고 10분 이상 재운다.",
            "양파, 당근, 대파는 채 썬다.",
            "달군 팬에 식용유를 두르고 양념한 고기를 센 불에서 볶는다.",
            "고기가 반쯤 익으면 채소와 남은 양념장을 넣고 함께 볶는다.",
            "고기가 완전히 익고 양념이 고루 배면 불을 끈다."
        ),
        List.of("한식", "매운맛", "집밥")
    ));

    list.add(new NormalizedRecipe(
        "계란찜",
        "부드럽고 폭신하게 쪄낸 계란찜. 자극적이지 않아 속을 편안하게 해주는 반찬.",
        2, 5, 15, 20, Difficulty.EASY, "반찬", "한식",
        null,
        List.of(
            ing("계란 4개"), ing("물 또는 육수 1컵"), ing("새우젓 1작은술"),
            ing("다진 대파 1큰술"), ing("당근 약간(선택)")
        ),
        List.of(
            "계란을 볼에 풀고 체에 한 번 걸러 알끈을 제거한다.",
            "물(또는 육수)과 새우젓을 넣고 잘 섞는다.",
            "뚝배기나 냄비에 계란물을 붓고 중약불에 올린다.",
            "나무젓가락으로 저어가며 몽글몽글 엉기기 시작하면 다진 대파를 넣는다.",
            "뚜껑을 덮고 약불에서 5분 정도 더 익혀 폭신하게 부풀리면 완성."
        ),
        List.of("한식", "순한맛", "반찬")
    ));

    list.add(new NormalizedRecipe(
        "된장찌개",
        "구수한 된장 베이스에 채소를 듬뿍 넣고 끓인 담백한 찌개. 편안하고 익숙한 집밥의 맛.",
        2, 10, 15, 25, Difficulty.EASY, "찌개", "한식",
        null,
        List.of(
            ing("된장 2큰술"), ing("두부 1/2모"), ing("애호박 1/3개"),
            ing("양파 1/2개"), ing("감자 1/2개"), ing("청양고추 1개"),
            ing("다진마늘 1작은술"), ing("멸치 육수 2컵")
        ),
        List.of(
            "멸치 육수를 냄비에 붓고 된장을 체에 걸러 풀어준다.",
            "감자를 먼저 넣고 중불에서 5분간 끓인다.",
            "양파, 애호박을 넣고 5분 더 끓인다.",
            "두부와 다진마늘을 넣고 3분 더 끓인다.",
            "청양고추를 어슷 썰어 넣고 한소끔 끓여 마무리한다."
        ),
        List.of("한식", "구수한맛", "집밥")
    ));

    list.add(new NormalizedRecipe(
        "잡채",
        "당면과 각종 채소, 고기를 간장 양념에 볶아 만드는 명절·잔치 단골 메뉴. 특별한 한 끼로 좋다.",
        4, 20, 20, 40, Difficulty.MEDIUM, "면요리", "한식",
        null,
        List.of(
            ing("당면 200g"), ing("소고기 채썬 것 100g"), ing("당근 1/2개"),
            ing("시금치 한 줌"), ing("표고버섯 3개"), ing("양파 1/2개"),
            ing("간장 4큰술"), ing("설탕 2큰술"), ing("참기름 2큰술"),
            ing("다진마늘 1큰술"), ing("깨소금 약간")
        ),
        List.of(
            "당면은 미지근한 물에 30분 불린 뒤 끓는 물에 삶아 찬물에 헹군다.",
            "소고기와 표고버섯은 간장·설탕·다진마늘로 밑간해 각각 볶는다.",
            "당근, 양파는 채 썰어 소금 간을 살짝 해서 볶고, 시금치는 데쳐 물기를 짠다.",
            "삶은 당면에 간장, 설탕, 참기름을 넣고 먼저 버무린다.",
            "볶아둔 재료를 모두 넣고 고루 섞은 뒤 깨소금을 뿌려 마무리한다."
        ),
        List.of("한식", "잔치음식", "명절")
    ));

    list.add(new NormalizedRecipe(
        "비빔밥",
        "여러 나물과 고추장을 밥 위에 올려 비벼 먹는 균형 잡힌 한 그릇. 있는 재료로 가볍게 즐기기 좋다.",
        1, 15, 10, 25, Difficulty.EASY, "밥", "한식",
        null,
        List.of(
            ing("밥 1공기"), ing("시금치나물 1/2컵"), ing("콩나물무침 1/2컵"),
            ing("당근채 볶음 1/4컵"), ing("계란 1개"), ing("고추장 1큰술"),
            ing("참기름 1작은술"), ing("깨소금 약간")
        ),
        List.of(
            "그릇에 밥을 담는다.",
            "시금치나물, 콩나물무침, 당근채 볶음을 밥 위에 색 맞춰 돌려 담는다.",
            "계란은 반숙 프라이로 부쳐 가운데 올린다.",
            "고추장과 참기름을 곁들이고 깨소금을 뿌린다.",
            "먹기 직전 골고루 비벼서 먹는다."
        ),
        List.of("한식", "건강식", "비빔")
    ));

    list.add(new NormalizedRecipe(
        "떡볶이",
        "쫄깃한 떡과 매콤달콤한 고추장 양념의 조합. 스트레스 풀리는 매운맛이 필요할 때.",
        2, 5, 15, 20, Difficulty.EASY, "분식", "한식",
        null,
        List.of(
            ing("떡볶이떡 300g"), ing("어묵 2장"), ing("대파 1/2대"),
            ing("멸치 육수 2컵"), ing("고추장 2큰술"), ing("고춧가루 1큰술"),
            ing("설탕 1.5큰술"), ing("간장 1큰술"), ing("다진마늘 1작은술")
        ),
        List.of(
            "떡은 찬물에 헹궈 붙지 않게 풀어둔다.",
            "냄비에 멸치 육수를 붓고 고추장, 고춧가루, 설탕, 간장, 다진마늘을 넣어 끓인다.",
            "양념 육수가 끓으면 떡과 어묵을 넣는다.",
            "중불에서 저어가며 국물이 걸쭉해질 때까지 5~7분간 졸인다.",
            "송송 썬 대파를 넣고 한 번 더 끓여 마무리한다."
        ),
        List.of("한식", "분식", "매운맛")
    ));

    list.add(new NormalizedRecipe(
        "미역국",
        "부드러운 미역을 소고기와 함께 끓여낸 담백한 국. 생일에 먹는 한국의 대표 축하 음식이자 편안한 집밥.",
        3, 10, 25, 35, Difficulty.EASY, "국", "한식",
        null,
        List.of(
            ing("마른 미역 20g"), ing("소고기 국거리 100g"), ing("참기름 1큰술"),
            ing("국간장 1큰술"), ing("다진마늘 1작은술"), ing("물 4컵")
        ),
        List.of(
            "마른 미역은 물에 20분간 불린 뒤 먹기 좋은 크기로 자른다.",
            "냄비에 참기름을 두르고 소고기를 볶다가 미역을 넣어 함께 볶는다.",
            "국간장과 다진마늘을 넣고 2분간 더 볶아 향을 낸다.",
            "물을 붓고 센 불로 끓이다가 끓어오르면 중약불로 줄여 20분간 끓인다.",
            "부족한 간은 국간장으로 맞춘다."
        ),
        List.of("한식", "담백한맛", "생일")
    ));

    return list;
  }
}
