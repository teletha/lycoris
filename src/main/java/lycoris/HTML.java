/*
 * Copyright (C) 2024 The JAVADNG Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package lycoris;

import java.util.function.Consumer;

import kiss.I;
import kiss.Variable;
import kiss.XML;
import stylist.Style;

/**
 * Domain Specific Language for HTML.
 */
public abstract class HTML extends Tree<String, XML> {

    public HTML() {
        super((name, id, context) -> {
            return I.xml("<" + name + "/>");
        }, null, (follower, current) -> {
            if (follower instanceof Style) {
                Style style = (Style) follower;
                for (String className : style.className()) {
                    current.addClass(className);
                }
            } else {
                follower.accept(current);
            }
        });
    }

    /**
     * Defines the structure and content of the HTML document or fragment.
     * 
     * <p>This abstract method serves as a blueprint for subclasses to implement
     * and define the specific elements, attributes, and hierarchy of the HTML structure.
     * It is typically invoked to initialize and build the desired HTML content.</p>
     * 
     * <p>Subclasses should override this method to declare the HTML nodes and their
     * relationships using the provided methods such as {@code $}, {@code attr}, and others.</p>
     */
    public abstract void declare();

    /**
     * Embeds the structure of another {@code HTML} instance into the current HTML document.
     * This method allows the reuse and composition of existing HTML definitions.
     * 
     * <p>The {@code declare()} method of the provided {@code HTML} instance is invoked to
     * generate its structure. The resulting XML nodes are then added to the current tree.</p>
     * 
     * @param html The {@code HTML} instance to be embedded. Its structure will be integrated
     *            into the current context.
     * @throws NullPointerException If the provided {@code HTML} instance is {@code null}.
     */
    protected final void $(HTML html) {
        html.declare();

        for (XML xml : html.root) {
            $(xml);
        }
    }

    /**
     * Creates a {@link Consumer} that sets an attribute on an HTML element.
     * 
     * <p>This method allows you to define an attribute for an HTML element by providing its name.
     * The attribute will be added to the target HTML node when the returned {@link Consumer}
     * is invoked.</p>
     * 
     * <p>If the provided {@code name} is {@code null} or an empty string, the attribute will
     * not be added.</p>
     * 
     * @param name The name of the attribute to be added. If {@code null} or empty, no
     *            attribute will be added.
     * @return A {@link Consumer} that can be used to set the specified attribute on an HTML node.
     */
    protected final Consumer attr(Object name) {
        return attr(name, null);
    }

    /**
     * Creates a {@link Consumer} that sets an attribute with a specified value on an HTML element.
     * 
     * <p>This method allows you to define an attribute and its value for an HTML element. The
     * attribute will be added to the target HTML node when the returned {@link Consumer}
     * is invoked.</p>
     * 
     * <p>If the provided {@code name} is {@code null} or an empty string, the attribute will
     * not be added.</p>
     * 
     * @param name The name of the attribute to be added. If {@code null} or empty, no
     *            attribute will be added.
     * @param value The value to be assigned to the attribute. If {@code null}, the attribute
     *            will still be added, but its value will be represented as {@code "null"}.
     * @return A {@link Consumer} that can be used to set the specified attribute and value
     *         on an HTML node.
     */
    protected final Consumer<XML> attr(Object name, Object value) {
        return parent -> {
            if (name != null) {
                String n = String.valueOf(name);

                if (!n.isEmpty()) {
                    parent.attr(n, String.valueOf(value));
                }
            }
        };
    }

    /**
     * Shorthand for charset attribute.
     * 
     * @param encoding
     * @return
     */
    protected final Consumer<XML> charset(String encoding) {
        return attr("charset", encoding);
    }

    /**
     * Shorthand for name attribute.
     * 
     * @param value
     * @return
     */
    protected final Consumer<XML> name(String value) {
        return attr("name", value);
    }

    /**
     * Shorthand for content attribute.
     * 
     * @param value
     * @return
     */
    protected final Consumer<XML> content(String value) {
        return attr("content", value);
    }

    /**
     * Shorthand for rel attribute.
     * 
     * @param value
     * @return
     */
    protected final Consumer<XML> rel(String value) {
        return attr("rel", value);
    }

    /**
     * Shorthand for href attribute.
     * 
     * @param value
     * @return
     */
    protected final Consumer<XML> href(String value) {
        return attr("href", value);
    }

    /**
     * Shorthand for src attribute.
     * 
     * @param value
     * @return
     */
    protected final Consumer<XML> src(String value) {
        return attr("src", value);
    }

    /**
     * Shorthand for id attribute.
     * 
     * @param value
     * @return
     */
    protected final Consumer<XML> id(String value) {
        return attr("id", value);
    }

    /**
     * Shorthand for class attribute.
     * 
     * @param value
     * @return
     */
    protected final Consumer<XML> clazz(String value) {
        return attr("class", value);
    }

    /**
     * Shorthand for title attribute.
     * 
     * @param value
     * @return
     */
    protected final Consumer<XML> title(String value) {
        return attr("title", value);
    }

    /**
     * Accept child node.
     * 
     * @param child
     * @return
     */
    protected final Consumer<XML> xml(XML child) {
        return parent -> parent.append(child);
    }

    /**
     * Build child node.
     * 
     * @param child
     * @return
     */
    protected final Consumer<XML> xml(Variable<XML> child) {
        return parent -> child.to(parent::append);
    }

    /**
     * Build code node.
     * 
     * @param text A text.
     */
    protected final Consumer<XML> code(Object text) {
        return parent -> parent.child("code").append(text);
    }

    /**
     * Build text node.
     * 
     * @param text A text.
     */
    protected final Consumer<XML> text(Object text) {
        return parent -> {
            parent.append(parent.to().getOwnerDocument().createTextNode(String.valueOf(text)));
        };
    }

    /**
     * Shorthand method to write stylesheet link tag.
     * 
     * @param uri URI to css.
     */
    protected final void stylesheet(String uri) {
        $("link", rel("stylesheet"), href(uri));
    }

    /**
     * Shorthand method to write stylesheet link tag.
     * 
     * @param uri URI to css.
     */
    protected final void stylesheetAsync(String uri) {
        $("link", rel("preload"), href(uri), attr("as", "style"), attr("fetchpriority", "high"));
        $("link", rel("stylesheet"), href(uri), attr("media", "print"), attr("onload", "this.media='all'"));
    }

    /**
     * Shorthand method to write script tag.
     * 
     * @param uri URI to script.
     */
    protected final void script(String uri) {
        $("script", attr("src", uri));
    }

    /**
     * Shorthand method to write script tag.
     * 
     * @param uri URI to script.
     */
    protected final void scriptAsync(String uri) {
        $("script", attr("src", uri), attr("async", true));
    }

    /**
     * Shorthand method to write script tag.
     * 
     * @param uri URI to script.
     */
    protected final void module(String uri) {
        $("script", attr("src", uri), attr("type", "module"));
    }

    /**
     * Shorthand method to write script tag.
     * 
     * @param uri URI to script.
     */
    protected final void moduleAsync(String uri) {
        $("script", attr("src", uri), attr("async", true), attr("type", "module"));
    }
}