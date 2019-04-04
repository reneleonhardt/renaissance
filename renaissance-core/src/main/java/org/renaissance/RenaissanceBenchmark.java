package org.renaissance;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

public abstract class RenaissanceBenchmark {
  public final String name() {
    String cn = this.getClass().getSimpleName();
    String camelCaseName =
      (cn.charAt(cn.length() - 1) == '$') ? cn.substring(0, cn.length() - 1) : cn;
    Pattern pattern = Pattern.compile("([A-Za-z])([A-Z])");
    // This functionality is duplicated in the kebabCase function of the build file.
    String result = camelCaseName;
    do {
      String last = result;
      result = pattern.matcher(result).replaceFirst("$1-$2");
      if (last == result) break;
    } while (true);
    return result.toLowerCase();
  }

  public final String mainGroup() {
    String fullName = getClass().getName();
    String simpleName = getClass().getSimpleName();
    String packageName = fullName.substring(0, fullName.indexOf(simpleName) - 1);
    String groupName = packageName.substring(packageName.lastIndexOf('.') + 1);
    return groupName;
  }

  public int defaultRepetitions() {
    return 20;
  }

  public abstract String description();

  public Optional<String> initialRelease() {
    return Optional.empty();
  }

  public Optional<String> finalRelease() {
    return Optional.empty();
  }

  protected void setUpBeforeAll(Config c) {
  }

  protected void tearDownAfterAll(Config c) {
  }

  protected void beforeIteration(Config c) {
  }

  protected void afterIteration(Config c) {
  }

  public final Optional<Throwable> runBenchmark(Config config) {
    try {
      setUpBeforeAll(config);
      if (!Policy.factories.containsKey(config.policy())) {
        System.err.println("Unknown policy " + config.policy() + ".");
        System.exit(1);
      }
      BiFunction<RenaissanceBenchmark, Config, Policy> factory =
        Policy.factories.get(config.policy());
      Policy policy = factory.apply(this, config);
      for (Plugin plugin : config.plugins()) {
        plugin.onStart(policy);
      }
      try {
        policy.execute();
      } finally {
        for (Plugin plugin : config.plugins()) {
          plugin.onTermination(policy);
        }
      }
      return Optional.empty();
    } catch (Throwable t) {
      return Optional.of(t);
    } finally {
      tearDownAfterAll(config);
    }
  }

  /**
   * This method runs the functionality of the benchmark.
   */
  protected abstract void runIteration(Config config);

  long runIterationWithBeforeAndAfter(Policy policy, Config config) {
    beforeIteration(config);

    for (Plugin plugin : config.plugins()) {
      plugin.beforeIteration(policy);
    }

    long start = System.nanoTime();

    runIteration(config);

    long end = System.nanoTime();
    long duration = end - start;

    for (Plugin plugin : config.plugins()) {
      plugin.afterIteration(policy, duration);
    }

    afterIteration(config);

    return duration;
  }

  public static class Dummy extends RenaissanceBenchmark {
    @Override
    public String description() {
      return "A dummy benchmark, which does no work.";
    }

    @Override
    protected void runIteration(Config config) {
    }
  }
}

