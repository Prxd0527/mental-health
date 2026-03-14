package com.mentalhealth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mentalhealth.entity.Question;
import com.mentalhealth.entity.Quiz;
import com.mentalhealth.entity.QuizResult;

import java.util.List;
import java.util.Map;

public interface QuizService extends IService<Quiz> {

    /**
     * 获取在线的问卷分页列表
     */
    Page<Quiz> getOnlineQuizPage(int pageNum, int pageSize);

    /**
     * 获取指定问卷的所有题目
     *
     * @param quizId 问卷ID
     */
    List<Question> getQuestionsByQuizId(Long quizId);

    /**
     * 提交问卷答卷并计算总分生成结果
     *
     * @param quizId  问卷ID
     * @param userId  答题用户ID
     * @param answers 题目ID对应的所选分值映射，如 { questionId : score }
     * @return 答卷结果实体
     */
    QuizResult submitQuiz(Long quizId, Long userId, Map<Long, Integer> answers);

    /**
     * 获取用户的测评历史记录
     */
    List<QuizResult> getUserQuizHistory(Long userId);
}
