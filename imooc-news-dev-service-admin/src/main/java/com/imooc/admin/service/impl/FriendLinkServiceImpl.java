package com.imooc.admin.service.impl;

import com.imooc.admin.repository.FriendLinkRepository;
import com.imooc.admin.service.FriendLinkService;
import com.imooc.enums.YesOrNo;
import com.imooc.pojo.mo.FriendLinkMO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FriendLinkServiceImpl implements FriendLinkService {
    @Autowired
    private FriendLinkRepository mongoRepository;
    @Override
    public void saveOrUpdateFriendLink(FriendLinkMO friendLinkMO) {
        mongoRepository.save(friendLinkMO);
    }

    @Override
    public List<FriendLinkMO> queryAllList() {

        return mongoRepository.findAll();
    }

    @Override
    public void delete(String linkId) {
        mongoRepository.deleteById(linkId);
    }

    @Override
    public List<FriendLinkMO> queryIndexAllList() {
        return mongoRepository.getAllByIsDelete(YesOrNo.NO.type);
    }
}
