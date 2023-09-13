package org.sk.fxcss;

/**
 *
 * @param uuid This is overdoing it. Adds some computing time, when a simple random generator would suffice
 *             Then again, the quick in quick'n dirty stands for speed of development, not of execution.
 * @param name
 * @param elementType
 * @param description
 * @param content
 */
public record Snippet(String uuid, String name, String elementType, String description,String content) {
}
