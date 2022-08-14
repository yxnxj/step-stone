package com.likelion.stepstone.stepstone;

import com.likelion.stepstone.like.LikeRepository;
import com.likelion.stepstone.like.LikeService;
import com.likelion.stepstone.like.model.LikeDto;
import com.likelion.stepstone.like.model.LikeEntity;
import com.likelion.stepstone.post.PostRepository;
import com.likelion.stepstone.post.model.PostEntity;
import com.likelion.stepstone.user.UserRepository;
import com.likelion.stepstone.user.model.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.UUID;

@SpringBootTest
class StepstoneApplicationTests {

	@Autowired
	private LikeRepository likeRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private PostRepository postRepository;
	@Autowired
	private LikeService likeService;

	@Test
	void contextLoads() {
	}

	@Test
	void likeRepositorySave() {

		UUID postId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();

		LikeEntity q1 = new LikeEntity();
		q1.setPostId(postId);
		q1.setUserId(userId);

		likeRepository.save(q1);


	}
	@Test
	void likeRepositorySave2() {

		UUID postId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();

		LikeEntity q2 = new LikeEntity();
		q2.setPostId(postId);
		q2.setUserId(userId);

		likeRepository.save(q2);


	}

	@Test
	public void like() {
		UserEntity user = userRepository.findByName("qwe");
		PostEntity post = postRepository.findByTitle("title");

		UUID userId = user.getUserId();
		UUID postId = post.getPostId();

		LikeDto likeDto = LikeDto.builder()
				.postId(postId)
				.userId(userId)
				.build();

		likeService.idCheck(postId,userId);

	}
	@Test
	public void addUserAndPost1(){
		UUID userId = UUID.randomUUID();
		UUID pw = UUID.randomUUID();

		UserEntity user1 = new UserEntity();

		user1.setName("No1");
		user1.setCreatedAt(LocalDateTime.now());
		user1.setPassword(pw);
		user1.setRole("user");
		user1.setUpdatedAt(LocalDateTime.now());
		user1.setUserId(userId);
		user1.setCid(1);
		userRepository.save(user1);


		UUID postId = UUID.randomUUID();

		PostEntity post1 = new PostEntity();

		post1.setPostId(postId);
		post1.setCreatedAt(LocalDateTime.now());
		post1.setTitle("title1");
		post1.setBody("body1");
		post1.setLikes(0);
		post1.setUpdatedAt(LocalDateTime.now());
		post1.setUserId(userId);
		post1.setPostCid(1L);
		postRepository.save(post1);

	}
	@Test
	public void addUserAndPost2() {
		UUID userId = UUID.randomUUID();
		UUID pw = UUID.randomUUID();

		UserEntity user4 = new UserEntity();

		user4.setName("No4");
		user4.setCreatedAt(LocalDateTime.now());
		user4.setPassword(pw);
		user4.setRole("user");
		user4.setUpdatedAt(LocalDateTime.now());
		user4.setUserId(userId);
		user4.setCid(2);
		userRepository.save(user4);


		UUID postId = UUID.randomUUID();

		PostEntity post4 = new PostEntity();

		post4.setPostId(postId);
		post4.setCreatedAt(LocalDateTime.now());
		post4.setTitle("title4");
		post4.setBody("body4");
		post4.setLikes(0);
		post4.setUpdatedAt(LocalDateTime.now());
		post4.setUserId(userId);
		post4.setPostCid(4L);
		postRepository.save(post4);

	}

	@Test
	public void addUserAndPost3() {
		UUID userId = UUID.randomUUID();
		UUID pw = UUID.randomUUID();

		UserEntity user3 = new UserEntity();

		user3.setName("No3");
		user3.setCreatedAt(LocalDateTime.now());
		user3.setPassword(pw);
		user3.setRole("user");
		user3.setUpdatedAt(LocalDateTime.now());
		user3.setUserId(userId);
		user3.setCid(3);
		userRepository.save(user3);


		UUID postId = UUID.randomUUID();

		PostEntity post3 = new PostEntity();

		post3.setPostId(postId);
		post3.setCreatedAt(LocalDateTime.now());
		post3.setTitle("title3");
		post3.setBody("body3");
		post3.setLikes(0);
		post3.setUpdatedAt(LocalDateTime.now());
		post3.setUserId(userId);
		post3.setPostCid(3L);
		postRepository.save(post3);

	}

}

