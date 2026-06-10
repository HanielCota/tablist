package com.hanielfialho.tablist.core.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.hanielfialho.tablist.core.model.Renderable;
import java.util.List;
import java.util.stream.LongStream;
import org.junit.jupiter.api.Test;

class AnimationFrameCyclerTest {

  @Test
  void cyclesThreeFramesEveryTwoTicksOverTenTicks() {
    AnimationFrameCycler cycler = AnimationFrameCycler.of(List.of("a", "b", "c"), 2);

    List<String> shown =
        LongStream.range(0, 10).mapToObj(cycler::frameAt).map(Renderable::template).toList();

    assertEquals(List.of("a", "a", "b", "b", "c", "c", "a", "a", "b", "b"), shown);
  }
}
