package com.lprevidente.orgcraft.office.domain;

import com.lprevidente.orgcraft.office.api.OfficeId;
import com.lprevidente.orgcraft.user.api.UserId;
import java.util.List;
import org.jmolecules.ddd.annotation.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface Offices extends JpaRepository<Office, OfficeId> {

  List<Office> findByCreator(UserId creator);
}
