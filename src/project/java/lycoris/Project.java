package lycoris;

/*
 * Copyright (C) 2023 The LYCORIS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
import static bee.api.License.*;

import javax.lang.model.SourceVersion;

public class Project extends bee.api.Project {
    {
        product("com.github.teletha", "lycoris", ref("version.txt"));
        license(MIT);
        require(SourceVersion.latest(), SourceVersion.RELEASE_21);

        require("com.github.teletha", "sinobu");
        require("com.github.teletha", "stylist");
        require("com.github.teletha", "antibug").atTest();

        describe("""
                Lycoris is designed to simplify the creation of hierarchical tree structures through a domain-specific language (DSL).
                It is versatile and reusable, making it ideal for managing nested or hierarchical data structures.

                ## Features
                - Flexible Node Construction: Nodes can hold values of any type.
                - Easy Hierarchical Definition: Create tree structures with concise and intuitive DSL syntax.
                - Conditional and Loop Support: Easily handle dynamic structures using `if` conditions and `for` loops.
                - Extensibility: Customize builders and processors to suit your specific needs.
                """);

        versionControlSystem("https://github.com/teletha/lycoris");
    }
}