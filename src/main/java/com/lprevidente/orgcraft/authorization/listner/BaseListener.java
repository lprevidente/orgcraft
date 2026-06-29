package com.lprevidente.orgcraft.authorization.listner;

import com.authzed.api.v1.ObjectReference;
import com.authzed.api.v1.Relationship;
import com.authzed.api.v1.RelationshipUpdate;
import com.authzed.api.v1.SubjectReference;

public abstract class BaseListener {


  protected static RelationshipUpdate touch(
      String resourceType,
      String resourceId,
      String relation,
      String subjectType,
      String subjectId) {
    return RelationshipUpdate.newBuilder()
        .setOperation(RelationshipUpdate.Operation.OPERATION_TOUCH)
        .setRelationship(Relationship.newBuilder()
            .setResource(ObjectReference.newBuilder().setObjectType(resourceType).setObjectId(resourceId))
            .setRelation(relation)
            .setSubject(SubjectReference.newBuilder()
                .setObject(ObjectReference.newBuilder().setObjectType(subjectType).setObjectId(subjectId))))
        .build();
  }
}
