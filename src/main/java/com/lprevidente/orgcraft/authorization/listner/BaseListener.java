package com.lprevidente.orgcraft.authorization.listner;

import com.authzed.api.v1.DeleteRelationshipsRequest;
import com.authzed.api.v1.ObjectReference;
import com.authzed.api.v1.PermissionsServiceGrpc.PermissionsServiceBlockingStub;
import com.authzed.api.v1.Relationship;
import com.authzed.api.v1.RelationshipFilter;
import com.authzed.api.v1.RelationshipUpdate;
import com.authzed.api.v1.SubjectFilter;
import com.authzed.api.v1.SubjectReference;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class BaseListener {

  protected final PermissionsServiceBlockingStub permissionsService;


  protected static RelationshipUpdate touch(
      String resourceType,
      String resourceId,
      String relation,
      String subjectType,
      String subjectId) {
    return update(
        RelationshipUpdate.Operation.OPERATION_TOUCH,
        resourceType,
        resourceId,
        relation,
        subjectType,
        subjectId);
  }

  protected static RelationshipUpdate delete(
      String resourceType,
      String resourceId,
      String relation,
      String subjectType,
      String subjectId) {
    return update(
        RelationshipUpdate.Operation.OPERATION_DELETE,
        resourceType,
        resourceId,
        relation,
        subjectType,
        subjectId);
  }

  /**
   * Deletes every relationship of {@code resourceType} whose subject is {@code
   * subjectType:subjectId}. The filter is resource-scoped, so issue one call per resource type.
   */
  protected static DeleteRelationshipsRequest deleteBySubject(
      String resourceType, String subjectType, String subjectId) {
    return DeleteRelationshipsRequest.newBuilder()
        .setRelationshipFilter(
            RelationshipFilter.newBuilder()
                .setResourceType(resourceType)
                .setOptionalSubjectFilter(
                    SubjectFilter.newBuilder()
                        .setSubjectType(subjectType)
                        .setOptionalSubjectId(subjectId)))
        .build();
  }

  private static RelationshipUpdate update(
      RelationshipUpdate.Operation operation,
      String resourceType,
      String resourceId,
      String relation,
      String subjectType,
      String subjectId) {
    return RelationshipUpdate.newBuilder()
        .setOperation(operation)
        .setRelationship(Relationship.newBuilder()
            .setResource(ObjectReference.newBuilder().setObjectType(resourceType).setObjectId(resourceId))
            .setRelation(relation)
            .setSubject(SubjectReference.newBuilder()
                .setObject(ObjectReference.newBuilder().setObjectType(subjectType).setObjectId(subjectId))))
        .build();
  }
}
