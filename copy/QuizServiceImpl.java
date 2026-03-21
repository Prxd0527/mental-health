package com.mentalhealth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mentalhealth.entity.Question;
import com.mentalhealth.entity.Quiz;
import com.mentalhealth.entity.QuizResult;
import com.mentalhealth.mapper.QuestionMapper;
import com.mentalhealth.mapper.QuizMapper;
import com.mentalhealth.mapper.QuizResultMapper;
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

    @Override
    public Page<Quiz> getOnlineQuizPage(int pageNum, int pageSize) {
        Page<Quiz> page = new Page<>(pageNum, pageSize);
        // 只查询已上线的问卷
        LambdaQueryWrapper<Quiz> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Quiz::getStatus, 1)
                .orderByDesc(Quiz::getCreateTime);
        return baseMapper.selectPage(page, wrapper);
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

        // 目前粗分配只计算累加总分
        int totalScore = 0;
        for (Integer score : answers.values()) {
            totalScore += score != null ? score : 0;
        }

        QuizResult result = new QuizResult();
        result.setQuizId(quizId);
        result.setUserId(userId);
        result.setTotalScore(totalScore);

        // 简述测试建议算法（后续可扩展配置化）
        if (totalScore < 30) {
            result.setSuggestion("您的心理状态非常棒，依然保持乐观开朗的心态！");
        } else if (totalScore < 60) {
            result.setSuggestion("您可能近期有些压力，建议劳逸结合，多参加放松活动。");
        } else {
            result.setSuggestion("【高危预警】您的心理压力得分过高，情况较为严峻。建议立即主动预约专业咨询师进行深度疏导。若遇到紧急状况，请立刻拨打24小时免费国家心理危机干预热线：400-161-9995。");
            System.err.println("!!! [系统高危告警] 用户 ID: " + userId + " 在问卷 ID: " + quizId + " 中得分: " + totalScore + "，触发高危预警阈值 !!!");
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
