package com.ozguryazilim.telve.idm.ldapSync;

import com.ozguryazilim.telve.messagebus.command.AbstractStorableCommand;

public class LdapSyncCommand extends AbstractStorableCommand {

    private Boolean syncGroupsAndAssignUsers;
    private Boolean createMissingGroups;
    private Boolean syncRolesAndAssignUsers;

    public LdapSyncCommand(Boolean syncGroupsAndAssignUsers, Boolean createMissingGroups, Boolean
            syncRolesAndAssignUsers) {
        this.syncGroupsAndAssignUsers = syncGroupsAndAssignUsers;
        this.createMissingGroups = createMissingGroups;
        this.syncRolesAndAssignUsers = syncRolesAndAssignUsers;
    }

    public Boolean getSyncGroupsAndAssignUsers() {
        return syncGroupsAndAssignUsers;
    }

    public void setSyncGroupsAndAssignUsers(Boolean syncGroupsAndAssignUsers) {
        this.syncGroupsAndAssignUsers = syncGroupsAndAssignUsers;
    }

    public Boolean getCreateMissingGroups() {
        return createMissingGroups;
    }

    public void setCreateMissingGroups(Boolean createMissingGroups) {
        this.createMissingGroups = createMissingGroups;
    }

    public Boolean getSyncRolesAndAssignUsers() {
        return syncRolesAndAssignUsers;
    }

    public void setSyncRolesAndAssignUsers(Boolean syncRolesAndAssignUsers) {
        this.syncRolesAndAssignUsers = syncRolesAndAssignUsers;
    }
}
