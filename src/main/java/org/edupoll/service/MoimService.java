package org.edupoll.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.edupoll.model.dto.moim.AttendanceRequestData;
import org.edupoll.model.dto.moim.MoimDetailResponseData;
import org.edupoll.model.dto.moim.MoimListResponseData;
import org.edupoll.model.dto.moim.MoimModifyRequestData;
import org.edupoll.model.dto.moim.MoimModifyResponseData;
import org.edupoll.model.dto.moim.MoimPageResponseData;
import org.edupoll.model.dto.reply.ReplyPageData;
import org.edupoll.model.dto.reply.ReplyPageResponseData;
import org.edupoll.model.dto.reply.ReplyResponseData;
import org.edupoll.model.dto.moim.MoimPageData;
import org.edupoll.model.entity.Attendance;
import org.edupoll.model.entity.Moim;
import org.edupoll.model.entity.Reply;
import org.edupoll.model.entity.User;
import org.edupoll.repository.AttendanceRepository;
import org.edupoll.repository.MoimRepository;
import org.edupoll.repository.ReplyRepository;
import org.edupoll.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

@Service
public class MoimService {
	
	@Autowired
	MoimRepository moimRepository;
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	AttendanceRepository attendanceRepository;
	
	@Autowired
	ReplyRepository replyRepository;
	
	// 모임 만들기
	public boolean createMoim(Moim moim, String logonId) {
		User user = userRepository.findById(logonId).get();
		moim.setManager(user);
		Moim create = moimRepository.save(moim);
		
		Attendance at = new Attendance();
		at.setMoim(moim);
		at.setUser(user);
		attendanceRepository.save(at);
		
		return true;
	}
	
	// 모임 페이징처리해서 가져오기
	public MoimPageResponseData findByMoimAll(int page) {
		Sort sort = Sort.by(Direction.ASC, "targetDate");
		
		List<Moim> list = moimRepository.findAll(PageRequest.of(page-1, 12, sort)).toList();
		
		int total = (int)moimRepository.count();
		int totalPage = total/12 + (total % 12 > 0 ? 1: 0);
		int viewPage = 5;
		int endPage = (((page-1)/viewPage)+1) * viewPage;
		int nextPage = 0;
		
		if(totalPage < endPage) {
		    endPage = totalPage;
		}
		int startPage = ((page-1)/viewPage) * viewPage + 1;
		
		int idx = Math.round(startPage / viewPage)+1;
		

		List<MoimPageData> pages = new ArrayList<>();
		for(int i=startPage; i<=5*idx; i++) {
			pages.add(new MoimPageData(String.valueOf(i), page == i));
			if(i == totalPage) {
				nextPage = i+1;
				break;
			}
			nextPage = i+1;
		}
				
		boolean existPrev = page >= 6;
		boolean existNext = true;
		if(endPage >= totalPage)
		{
			existNext = false;
		}
		
		List<MoimListResponseData> moims = list.stream().map(MoimListResponseData::new).toList();
		
		MoimPageResponseData moimsPage = new MoimPageResponseData
				(pages, viewPage, nextPage, startPage, existPrev, existNext, moims);
		
		return moimsPage;
	}
	
	// 모임 디테일 가져오기
	public MoimDetailResponseData findByMoim(String moimId, String logonId, int page) {
		// 모임이 있는지 없는지 체크한다.
		Optional<Moim> option = moimRepository.findById(moimId);
		if(option.isEmpty()) {
			return null;
		}
		
		// 모임객체로 만든다
		Moim moim = option.get();
		
		// 정렬 기준으로 페이징처리하여 댓글 리스트를 뽑는다
		Sort sort = Sort.by(Direction.ASC ,"dates");
		List<ReplyResponseData> replies = replyRepository.findByMoimId(moimId, PageRequest.of(page-1, 10, sort))
			.stream().map(ReplyResponseData::new).toList();
		
		// 화면 페이징처리를 계산한다.
		int total = (int)replyRepository.countByMoimId(moimId);
		int totalPage = total/10 + (total % 10 > 0 ? 1: 0);
		int viewPage = 5;
		int endPage = (((page-1)/viewPage)+1) * viewPage;
		int nextPage = 0;
		
		if(totalPage < endPage) {
		    endPage = totalPage;
		}
		int startPage = ((page-1)/viewPage) * viewPage + 1;
		
		int idx = Math.round(startPage / viewPage)+1;
		
		List<ReplyPageData> pages = new ArrayList<>();
		for(int i=startPage; i<=5*idx; i++) {
			pages.add(new ReplyPageData(String.valueOf(i), page == i));
			if(i == totalPage) {
				nextPage = i+1;
				break;
			}
			nextPage = i+1;
		}
				
		boolean existPrev = page >= 6;
		boolean existNext = true;
		if(endPage >= totalPage)
		{
			existNext = false;
		}
		
		// 댓글 리스트와 화면용 페이지 리스트를 담을 댓글 최종객체
		ReplyPageResponseData pageReplies = new ReplyPageResponseData
				(replies, pages, viewPage, nextPage, startPage-1, existPrev, existNext);
		
		boolean joined = attendanceRepository.existsByUserIdIsAndMoimIdIs(logonId, moimId);
		boolean creator = moim.getManager().getId().equals(logonId);
		System.out.println("작성자 : "+ moim.getManager().getId() + "/" + logonId +"/"+ creator);
		
		MoimDetailResponseData moimData = new MoimDetailResponseData
				(moim, pageReplies, joined, creator);
		
		return moimData;
	}
	
	// 모임 삭제
	public boolean deleteByMoim(String moimId) {
		Optional<Moim> moimOption = moimRepository.findById(moimId);
		if(moimOption.isEmpty()) {
			return false;
		}
		
		Moim moim = moimOption.get();
		List<Reply> replys = moim.getReplys();
		List<Attendance> attendancens = moim.getAttendances();
		
		if(!replys.isEmpty()) {
			for(Reply r : replys) {
				replyRepository.deleteById(r.getId());
			}			
		}
		
		if(!attendancens.isEmpty()) {
			for(Attendance a : attendancens) {
				attendanceRepository.deleteById(a.getId());
			}
		}
		
		moimRepository.delete(moim);
		
		return true;
	}
	
	// 모임 수정을 위한 모임정보 가져오기(댓글X 참가자X)
	public MoimModifyResponseData modifyByMoim(String moimId) {
		
		Moim moim = moimRepository.findById(moimId).get();
		
		return new MoimModifyResponseData(moim);
	}
}
