package com.mahmoud.incident_service.Models.incident;

public enum Status {
    PENDING, IN_PROGRESS, RESOLVED, ASSIGNED
}

//pending then when the man in the loop assigns a unit it becomes assigned,
// then when the dispatch service confirms the start iof the action is becomes in progresa dn when the unit returns to statioin it ginally becomes resolved