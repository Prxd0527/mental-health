package com.mentalhealth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mentalhealth.entity.Question;
import com.mentalhealth.entity.Quiz;
import com.mentalhealth.entity.QuizResult;
import com.mentalhealth.entity.QuizRule;
import com.mentalhealth.mapper.QuestionMapper;
import com.mentalhealth.mapper.QuizMapper;
import com.mentalhealth.mapper.QuizResultMapper;
import com.mentalhealth.mapper.QuizRuleMapper;
import com.mentalhealth.service.QuizService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
public class QuizServiceImpl extends ServiceImpl<QuizMapper, Quiz> implements QuizService {

    @Resource
    private QuestionMapper questionMapper;

    @Resource
    private QuizResultMapper quizResultMapper;

    @Resource
    private QuizRuleMapper quizRuleMapper;

    @Override
    public Page<Quiz> getOnlineQuizPage(int pageNum, int pageSize) {
        Page<Quiz> page = new Page<>(pageNum, pageSize);
        // 只查询已上线的问卷
        LambdaQueryWrapper<Quiz> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Quiz::getStatus, 1)
                .orderByDesc(Quiz::getCreateTime);
        Page<Quiz> result = baseMapper.selectPage(page, wrapper);

        // 填充每个量表的题目数量
        for (Quiz quiz : result.getRecords()) {
            Long count = questionMapper.selectCount(new LambdaQueryWrapper<Question>()
                    .eq(Question::getQuizId, quiz.getId()));
            quiz.setQuestionCount(count.intValue());
        }

        return result;
    }

    @Override
    public List<Question> getQuestionsByQuizId(Long quizId) {
        return questionMapper.selectList(new LambdaQueryWrapper<Question>()
                .eq(Question::getQuizId, quizId)
                .orderByAsc(Question::getSortOrder));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QuizResult submitQuiz(Long quizId, Long userId, Map<Long, Integer> answers) {
        if (answers == null || answers.isEmpty()) {
            throw new RuntimeException("答卷内容不能为空");
        }

        Quiz quiz = this.getById(quizId);
        if (quiz == null || quiz.getStatus() == 0) {
            throw new RuntimeException("问卷不存在或已下架");
        }

        // 累加总分
        int totalScore = 0;
        for (Integer score : answers.values()) {
            totalScore += score != null ? score : 0;
        }

        QuizResult result = new QuizResult();
        result.setQuizId(quizId);
        result.setUserId(userId);
        result.setTotalScore(totalScore);

        // 从 quiz_rule 表动态匹配评分区间，生成建议
        List<QuizRule> rules = quizRuleMapper.selectList(new LambdaQueryWrapper<QuizRule>()
                .eq(QuizRule::getQuizId, quizId)
                .le(QuizRule::getMinScore, totalScore)
                .ge(QuizRule::getMaxScore, totalScore));

        if (rules != null && !rules.isEmpty()) {
            result.setSuggestion(rules.get(0).getConclusion());
        } else {
            // 兜底：无匹配规则时使用通用建议
            result.setSuggestion("测评已完成，总分：" + totalScore + " 分。建议咨询专业人士获取更详细的解读。");
        }

        // 低分预警日志（分值越低=心理困扰越严重）
        // 根据量表满分动态计算阈值（题目数 × 最高选项分 × 40%）
        Long qCount = questionMapper.selectCount(new LambdaQueryWrapper<Question>().eq(Question::getQuizId, quizId));
        int maxPossible = (int) (qCount * 4); // 每题最高4分
        int warningThreshold = (int) (maxPossible * 0.4); // 低于满分 40% 触发告警
        if (totalScore <= warningThreshold) {
            System.err.println("!!! [系统高危告警] 用户 ID: " + userId + " 在问卷 ID: " + quizId + " 中得分: " + totalScore + "/" + maxPossible + "（低分预警），触发高危预警阈值 !!!");
        }

        quizResultMapper.insert(result);

        // 将标题回填展示
        result.setQuizTitle(quiz.getTitle());
        return result;
    }

    @Override
    public List<QuizResult> getUserQuizHistory(Long userId) {
        List<QuizResult> list = quizResultMapper.selectList(new LambdaQueryWrapper<QuizResult>()
                .eq(QuizResult::getUserId, userId)
                .orderByDesc(QuizResult::getCreateTime));

        // 填充关联问卷标题
        for (QuizResult res : list) {
            Quiz quiz = this.getById(res.getQuizId());
            if (quiz != null) {
                res.setQuizTitle(quiz.getTitle());
            }
        }

        return list;
    }

    @Override
    public List<Question> getQuestions(Long quizId) {
        return getQuestionsByQuizId(quizId);
    }

    @Override
    public void saveOrUpdateQuestion(Question question) {
        if (question.getId() != null) {
            questionMapper.updateById(question);
        } else {
            questionMapper.insert(question);
        }
    }

    @Override
    public void removeQuestionById(Long questionId) {
        questionMapper.deleteById(questionId);
    }
}
