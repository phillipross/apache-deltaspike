/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.deltaspike.data.test.service;

import static jakarta.persistence.LockModeType.PESSIMISTIC_READ;

import org.apache.deltaspike.data.api.EntityManagerDelegate;
import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Modifying;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.Repository;
import org.apache.deltaspike.data.test.domain.Simple;

@Repository
public interface ExtendedRepositoryInterface extends EntityRepository<Simple, Long>, EntityManagerDelegate<Simple>
{

    @Query(lock = PESSIMISTIC_READ)
    Simple findByName(String name);

    @Query(named = Simple.BY_NAME_LIKE)
    Simple findByNameNoLock(String name);

    @Modifying @Query("delete from Simple")
    int deleteAll();

}
