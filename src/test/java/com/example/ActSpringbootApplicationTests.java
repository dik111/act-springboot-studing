package com.example;

import com.example.utils.SecurityUtil;
import org.activiti.api.process.model.ProcessDefinition;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.builders.ClaimTaskPayloadBuilder;
import org.activiti.api.task.model.builders.TaskPayloadBuilder;
import org.activiti.api.task.model.payloads.ClaimTaskPayload;
import org.activiti.api.task.runtime.TaskRuntime;
import org.activiti.engine.RepositoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ActSpringbootApplicationTests {

    @Autowired
    private ProcessRuntime processRuntime;

    @Autowired
    private TaskRuntime taskRuntime;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private SecurityUtil securityUtil;



    @Test
    void contextLoads() {
        System.out.println(taskRuntime);
    }

    @Test
    public void test02(){
        securityUtil.logInAs("system");
        Page<ProcessDefinition> processDefinitionPage = processRuntime.processDefinitions(Pageable.of(0, 10));
        System.out.println("可用的流程定义数量: "+processDefinitionPage.getTotalItems());
        for (ProcessDefinition processDefinition : processDefinitionPage.getContent()) {
            System.out.println("流程定义："+processDefinition);
        }
    }

    @Test
    public void test03(){
        repositoryService.createDeployment()
                .addClasspathResource("processes/my-evection.bpmn")
                .addClasspathResource("processes/my-evection.png")
                .name("出差申请单")
                .deploy();
    }

    @Test
    public void test04(){
        securityUtil.logInAs("system");
        ProcessInstance processInstance = processRuntime.start(ProcessPayloadBuilder
                .start()
                .withProcessDefinitionKey("my-evection")
                .build());

        System.out.println("流程实例ID： "+processInstance.getId());
    }

    /**
     * 任务查询，拾取及完成操作
     */
    @Test
    public void test05(){
        securityUtil.logInAs("jack");
        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0, 10));
        if (tasks != null && tasks.getTotalItems()>0){
            for (Task task : tasks.getContent()) {
                // 拾取任务
                taskRuntime.claim(TaskPayloadBuilder
                        .claim()
                        .withTaskId(task.getId())
                        .build());
                System.out.println("任务："+task);
                taskRuntime.complete(TaskPayloadBuilder
                        .complete()
                        .withTaskId(task.getId())
                        .build());
            }
        }
        Page<Task> taskPage2 = taskRuntime.tasks(Pageable.of(0, 10));
        if (taskPage2.getTotalItems()>0){
            System.out.println("任务："+taskPage2.getContent());
        }
    }

}
