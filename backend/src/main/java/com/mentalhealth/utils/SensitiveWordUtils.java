package com.mentalhealth.utils;

import cn.hutool.dfa.WordTree;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * DFA 敏感词过滤工具
 */
@Component
public class SensitiveWordUtils {

    // Hutool 提供的 DFA 单词树
    private final WordTree wordTree = new WordTree();

    @PostConstruct
    public void init() {
        // 项目实际中应从数据库或配置文件读取敏感词库，这里预置几个常见的用于演示
        wordTree.addWord("强奸");
        wordTree.addWord("自杀");
        wordTree.addWord("砍人");
        wordTree.addWord("毒药");
        wordTree.addWord("微信转账");
    }

    /**
     * 判断是否包含敏感词
     */
    public boolean containsSensitiveWord(String text) {
        return wordTree.isMatch(text);
    }

    /**
     * 将敏感词替换为等长 * 号
     */
    public String replaceSensitiveWord(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }

        String result = text;
        List<String> matchedWords = wordTree.matchAll(text, -1, false, false);
        for (String word : matchedWords) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < word.length(); i++) {
                sb.append('*');
            }
            result = result.replace(word, sb.toString());
        }
        return result;
    }

    /**
     * 动态添加敏感词 (供后台管理调用)
     */
    public void addWord(String word) {
        wordTree.addWord(word);
    }

    /**
     * 初始化/重置敏感词库 (供后台管理调用更新)
     */
    public void resetWords(Iterable<String> words) {
        wordTree.clear();
        // Hutool WordTree.addWords() 需要 Collection, 此处做转换
        java.util.List<String> list = new java.util.ArrayList<>();
        words.forEach(list::add);
        wordTree.addWords(list);
    }
}
