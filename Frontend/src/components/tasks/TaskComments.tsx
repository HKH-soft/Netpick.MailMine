// TaskComments.tsx
"use client";
import React, { useState, useEffect } from "react";
import { FaComment, FaReply, FaTrash } from "react-icons/fa";
import commentService, { Comment } from "@/services/commentService";
import { useToast } from "@/context/ToastContext";

interface TaskCommentsProps {
  taskId: string;
}

export default function TaskComments({ taskId }: TaskCommentsProps) {
  const [comments, setComments] = useState<Comment[]>([]);
  const [newComment, setNewComment] = useState("");
  const [replyingTo, setReplyingTo] = useState<string | null>(null);
  const { addToast } = useToast();

  const loadComments = async () => {
    try {
      const data = await commentService.getByTask(taskId);
      setComments(data);
    } catch {
      addToast("error", "Error", "Failed to load comments");
    }
  };

  useEffect(() => {
    loadComments();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [taskId]);

  const handleAddComment = async () => {
    if (!newComment.trim()) return;
    try {
      await commentService.create({ taskId, content: newComment, authorId: "current-user" });
      setNewComment("");
      await loadComments();
    } catch {
      addToast("error", "Error", "Failed to add comment");
    }
  };

  const handleReply = async (parentId: string) => {
    const content = (document.getElementById(`reply-${parentId}`) as HTMLTextAreaElement)?.value;
    if (!content) return;
    try {
      await commentService.reply(parentId, taskId, "current-user", content);
      setReplyingTo(null);
      await loadComments();
    } catch {
      addToast("error", "Error", "Failed to reply");
    }
  };

  const handleDelete = async (id: string) => {
    try {
      await commentService.delete(id);
      await loadComments();
    } catch {
      addToast("error", "Error", "Failed to delete comment");
    }
  };

  const topLevelComments = comments.filter(c => !c.parentId);
  const replies = (parentId: string) => comments.filter(c => c.parentId === parentId);

  return (
    <div className="space-y-4">
      <div className="flex items-start gap-2">
        <textarea
          value={newComment}
          onChange={(e) => setNewComment(e.target.value)}
          placeholder="Add a comment..."
          className="flex-1 p-2 border rounded dark:bg-gray-800 dark:border-gray-600"
          rows={3}
        />
        <button
          onClick={handleAddComment}
          className="btn btn-primary"
        >
          <FaComment className="h-4 w-4" />
        </button>
      </div>
      <div className="space-y-3">
        {topLevelComments.map(comment => (
          <div key={comment.id} className="border-b pb-3">
            <div className="flex justify-between items-start">
              <p className="text-sm">{comment.content}</p>
              <div className="flex gap-2">
                <button onClick={() => setReplyingTo(comment.id)}>
                  <FaReply className="h-3 w-3" />
                </button>
                <button onClick={() => handleDelete(comment.id)}>
                  <FaTrash className="h-3 w-3" />
                </button>
              </div>
            </div>
            {replyingTo === comment.id && (
              <div className="mt-2">
                <textarea
                  id={`reply-${comment.id}`}
                  placeholder="Write reply..."
                  className="w-full p-2 border rounded text-sm"
                  rows={2}
                />
                <button onClick={() => handleReply(comment.id)} className="btn btn-primary btn-sm mt-1">
                  Reply
                </button>
              </div>
            )}
            {replies(comment.id).map(reply => (
              <div key={reply.id} className="ml-6 mt-2 p-2 bg-gray-50 dark:bg-gray-800 rounded">
                <p className="text-xs">{reply.content}</p>
              </div>
            ))}
          </div>
        ))}
      </div>
    </div>
  );
}