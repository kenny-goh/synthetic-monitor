package com.gkh.syntheticmonitor.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Data
@Builder
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class SyntheticTest {

	private String name;

	@Value("${apitest.scheduleTimeInMinute}")
	private int scheduleTimeInMinutes;

	@Value("${apitest.pauseTimeBetweenActionsInSeconds}")
	private int pauseTimeBetweenActionsInSeconds;

	private List<SyntheticTestActionInterface> actions;

	public static class SyntheticTestBuilder {
		public SyntheticTestBuilder apiTestAction(String name, String method, String url, HashMap headers, String preRequestScript, String postRequestScript) {
			this.actions.add(SyntheticTestActionAPI.builder()
					.name(name)
					.method(method)
					.url(url)
					.headers(headers)
					.preRequestScript(preRequestScript)
					.postRequestScript(postRequestScript)
					.build());
			return this;
		}

		public SyntheticTestBuilder apiTestAction(String name, String method, String url, String preRequestScript, String postRequestScript) {
			this.apiTestAction(name, method, url, new HashMap<>(), preRequestScript, postRequestScript);
			return this;
		}
	}

	public void execute(TestExecutionContext context) {
		context.getSyntheticTestResult().setName(this.name);
		var size = actions.size();
		for (var each : actions) {
			log.debug("Executing action {}", each.getName());
			try {
				each.preExecuteScript(context);
				each.execute(context);
				each.postExecuteScript(context);
				var index = actions.indexOf(each);
				if (index < size - 1) {
					TimeUnit.SECONDS.sleep(this.pauseTimeBetweenActionsInSeconds);
				}
			} catch (Exception e) {
				log.error("Error encountered. Message {}", e.getStackTrace());
				break;
			}
		}
	}


	public static String toYAML(SyntheticTest syntheticTest) {
		Representer representer = new Representer();
		representer.addClassTag(SyntheticTest.class, new Tag("!SyntheticTest"));
		representer.addClassTag(SyntheticTestActionAPI.class, new Tag("!ActionAPI"));
		representer.addClassTag(SyntheticTestActionNOOP.class, new Tag("!ActionNOOP"));

		DumperOptions options = new DumperOptions();
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		options.setPrettyFlow(true);

		Yaml yaml = new Yaml(representer, options);
		String result = yaml.dump(syntheticTest);
		return result;
	}


	public static SyntheticTest fromYAML(InputStream stream) throws IOException {
		Constructor constructor = new Constructor();
		constructor.addTypeDescription(new TypeDescription(SyntheticTest.class, "!SyntheticTest"));
		constructor.addTypeDescription(new TypeDescription(SyntheticTestActionAPI.class, "!ActionAPI"));
		constructor.addTypeDescription(new TypeDescription(SyntheticTestActionNOOP.class, "!NOOP"));

		Yaml yaml = new Yaml(constructor);

		return (SyntheticTest) yaml.load(stream);
	}

}
