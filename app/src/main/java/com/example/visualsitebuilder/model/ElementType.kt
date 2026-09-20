// Element type for design canvas items, each mapped to its HTML tag.
package com.example.visualsitebuilder.model

enum class ElementType(val label: String, val htmlTag: String) {
    TEXT("Text", "p"),
    HEADING_1("H1", "h1"),
    HEADING_2("H2", "h2"),
    HEADING_3("H3", "h3"),
    HEADING_4("H4", "h4"),
    HEADING_5("H5", "h5"),
    HEADING_6("H6", "h6"),
    LINK("Link", "a"),
    LABEL("Label", "label"),
    DIVIDER("Line", "hr"),
    IMAGE("Image", "img"),
    VIDEO("Video", "video"),
    BUTTON("Button", "button"),
    INPUT("Input", "input"),
    TEXTAREA("Area", "textarea"),
    CONTAINER("Box", "div"),
    SECTION("Section", "section"),
    HEADER("Header", "header"),
    FOOTER("Footer", "footer"),
    NAV("Nav", "nav"),
    MAIN("Main", "main"),
    ARTICLE("Article", "article"),
    FORM("Form", "form"),
    TABLE("Table", "table"),
    LIST("List", "ul"),
    ORDERED_LIST("Ordered", "ol"),
    LIST_ITEM("Item", "li");

    companion object {
        fun isHeading(type: ElementType): Boolean =
            type in setOf(HEADING_1, HEADING_2, HEADING_3, HEADING_4, HEADING_5, HEADING_6)

        fun isContainer(type: ElementType): Boolean =
            type in setOf(CONTAINER, SECTION, HEADER, FOOTER, NAV, MAIN, ARTICLE, FORM, TABLE, LIST, ORDERED_LIST)

        fun showsTextField(type: ElementType): Boolean =
            type != IMAGE && type != DIVIDER && type != VIDEO && !isContainer(type)

        fun showsFontSize(type: ElementType): Boolean = showsTextField(type)

        fun showsLinkField(type: ElementType): Boolean = type == LINK || type == VIDEO

        fun showsHintField(type: ElementType): Boolean = type == INPUT || type == TEXTAREA
    }
}
