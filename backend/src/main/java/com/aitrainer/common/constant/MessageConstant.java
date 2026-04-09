package com.aitrainer.common.constant;

/**
 * 信息常量类
 */
public final class MessageConstant {

    private MessageConstant() {}

    public static final String LOGIN_FAILED = "账号或密码错误";
    public static final String USERNAME_ALREADY_EXISTS = "用户名已存在";
    public static final String EMAIL_ALREADY_EXISTS = "电子邮箱已被注册";
    public static final String REGISTER_SUCCESS = "注册成功！请登录以继续。";
    public static final String VERIFY_CODE_ERROR = "验证码错误或已失效";
    public static final String VERIFY_CODE_SENT = "验证码已发送，请查收邮箱";
    public static final String EMAIL_NOT_REGISTERED = "该邮箱未注册";

    public static final String USER_NOT_FOUND = "无法找到当前登录用户";
    public static final String USER_NOT_LOGGED_IN = "用户未登录";

    public static final String PASSWORD_INCORRECT = "当前密码不正确";
    public static final String PASSWORD_SAME_AS_OLD = "新密码不能与当前密码相同";

    public static final String OSS_USER_ID_EMPTY = "用户 ID 不能为空";
    public static final String OSS_CONFIG_INCOMPLETE = "OSS 配置不完整，请检查环境变量配置";

    public static final String AVATAR_FILE_EMPTY = "请选择要上传的头像文件";
    public static final String AVATAR_FILE_TOO_LARGE = "头像文件不能超过 2MB";
    public static final String AVATAR_TYPE_NOT_SUPPORTED = "头像格式不支持，仅支持 JPG/PNG/WEBP/GIF";
    public static final String AVATAR_UPLOAD_FAILED = "头像上传失败，请稍后重试";

    public static final String ALREADY_FOLLOWED = "已关注该用户";
    public static final String NOT_FOLLOWED = "未关注该用户";
    public static final String CANNOT_FOLLOW_SELF = "不能关注自己";

    public static final String POST_CANNOT_BE_EMPTY = "推文内容不能为空";

    public static final String COMMENT_NOT_FOUND = "评论不存在或已被删除";
    public static final String COMMENT_DELETE_FORBIDDEN = "权限不足，只有作者或推文主人可以删除评论";
    public static final String POST_NOT_FOUND = "推文不存在或无权操作";
    public static final String COMMENT_REPLY_FAILED = "回复评论失败，原评论可能已被删除";
    public static final String WORKOUT_REPORT_NOT_FOUND = "战报不存在或无权操作";

    public static final String FOLDER_NAME_EMPTY = "收藏夹名称为空";
    public static final String FOLDER_NUMS_LIMITS = "收藏夹数量已达上限，请删除不常用的再试";
    public static final String FOLDER_NOT_FOUND = "收藏夹不存在或无权操作";

    public static final String USER_PROFILE_NOT_FOUND = "该用户资料不存在";

    // --- 留言板相关 ---
    public static final String GUESTBOOK_NOT_FOUND = "留言不存在或已被删除";
    public static final String GUESTBOOK_REPLY_FORBIDDEN = "只有空间主人可以回复留言";
    public static final String GUESTBOOK_DELETE_FORBIDDEN = "权限不足，无法删除该留言";
    public static final String GUESTBOOK_WITHDRAW_FORBIDDEN = "只有空间主人可以撤回回复";
    public static final String CANNOT_LEAVE_MSG_TO_SELF = "不能给自己留言哦，去社区发推文吧！";
}
