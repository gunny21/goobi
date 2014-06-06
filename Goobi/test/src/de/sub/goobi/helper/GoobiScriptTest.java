package de.sub.goobi.helper;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.easymock.EasyMock;
import org.goobi.beans.Docket;
import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.beans.User;
import org.goobi.beans.Usergroup;

import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.persistence.managers.StepManager;
import de.sub.goobi.persistence.managers.UserManager;
import de.sub.goobi.persistence.managers.UsergroupManager;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ StepManager.class, UserManager.class, UsergroupManager.class })
public class GoobiScriptTest {

    private List<Process> processList;

    @Before
    public void setUp() throws Exception {
        processList = new ArrayList<>();
        // process
        Process process = new Process();
        process.setTitel("process");
        process.setId(1);
        process.setDocket(new Docket());
        process.setDocketId(0);
        process.setMetadatenKonfigurationID(0);
        process.setIstTemplate(false);

        List<Step> stepList = new ArrayList<>();
        Step first = new Step();
        first.setReihenfolge(1);
        first.setTitel("first");
        first.setBearbeitungsstatusEnum(StepStatus.DONE);

        stepList.add(first);

        Step second = new Step();
        second.setReihenfolge(2);
        second.setTitel("second");
        second.setBearbeitungsstatusEnum(StepStatus.DONE);

        stepList.add(second);
        process.setSchritte(stepList);
        processList.add(process);

        prepareMocking();
    }

    private void prepareMocking() throws Exception {
        // swapSteps
        PowerMock.mockStatic(StepManager.class);
        StepManager.saveStep(EasyMock.anyObject(Step.class));
        StepManager.saveStep(EasyMock.anyObject(Step.class));

        // addUser
        User user = new User();
        user.setLogin("test");
        user.setId(0);
        List<User> userList = new ArrayList<>();
        userList.add(user);
        PowerMock.mockStatic(UserManager.class);
        EasyMock.expect(UserManager.getUsers(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyInt(), EasyMock.anyInt())).andReturn(userList)
                .anyTimes();

        // addUsergroup
        Usergroup group = new Usergroup();
        group.setId(1);
        group.setTitel("title");
        group.setBerechtigung(3);
        List<Usergroup> usergroupList = new ArrayList<>();
        usergroupList.add(group);
        PowerMock.mockStatic(UsergroupManager.class);
        EasyMock.expect(UsergroupManager.getUsergroups(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyInt(), EasyMock.anyInt())).andReturn(usergroupList)
        .anyTimes();
        
        
        PowerMock.replay(UsergroupManager.class);
        PowerMock.replay(UserManager.class);
        PowerMock.replay(StepManager.class);
    }

    @Test
    public void testConstructor() {
        GoobiScript script = new GoobiScript();
        assertNotNull(script);
    }

    @Test
    public void testExecuteEmptyGoobiScript() {
        GoobiScript script = new GoobiScript();
        script.execute(new ArrayList<Process>(), "");
        assertEquals(0, script.myParameters.size());
    }

    @Test
    public void testExecuteWrongSyntax() {
        GoobiScript script = new GoobiScript();
        script.execute(new ArrayList<Process>(), "action");
        assertEquals(0, script.myParameters.size());
    }

    @Test
    public void testExecuteUnknownAction() {
        GoobiScript script = new GoobiScript();
        script.execute(new ArrayList<Process>(), "action:test");
        assertEquals(1, script.myParameters.size());
    }

    @Test
    public void testExecuteSwapStepsAction() {
        GoobiScript script = new GoobiScript();
        script.execute(processList, "action:swapSteps");
        script.execute(processList, "action:swapSteps swap1nr:1");
        script.execute(processList, "action:swapSteps swap1nr:1 swap2nr:2");
        script.execute(processList, "action:swapSteps swap1nr:1 swap2nr:2 swap1title:first");
        script.execute(processList, "action:swapSteps swap1nr:NoNumber swap2nr:2 swap1title:first swap2title:second");
        script.execute(processList, "action:swapSteps swap1nr:1 swap2nr:2 swap1title:first swap2title:second");
    }

    @Test
    public void testExecuteSwapProzessesOutAction() {
        GoobiScript script = new GoobiScript();
        script.execute(processList, "action:swapProzessesOut");
    }

    @Test
    public void testExecuteSwapProzessesInAction() {
        GoobiScript script = new GoobiScript();
        script.execute(processList, "action:swapProzessesIn");
    }

    @Test
    public void testExecuteAddUserAction() {
        GoobiScript script = new GoobiScript();
        script.execute(processList, "action:addUser");
        script.execute(processList, "action:addUser steptitle:first");
        script.execute(processList, "action:addUser steptitle:first username:user");
    }

    @Test
    public void testExecuteAddUserGroupAction() {
        GoobiScript script = new GoobiScript();
        script.execute(processList, "action:addUserGroup");
        script.execute(processList, "action:addUserGroup steptitle:first");

        script.execute(processList, "action:addUserGroup steptitle:first group:group");
    }

}