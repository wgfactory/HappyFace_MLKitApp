package com.wgf.happyfaceapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.util.Log
import android.widget.Toast
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.face.Face
import com.google.android.gms.vision.face.FaceDetector

/**
 * Created by shailshah on 10/23/17.
 */
object FaceEmojifier {

    private val TAG = FaceEmojifier::class.java.simpleName

    private const val EMOJI_SCALE_FACTOR = 0.9f
    private const val SMILING_PROB_THRESHOLD = 0.15
    private const val EYE_OPEN_PROB_THRESHOLD = 0.5

    public var mSmileValue : Float = 0.0f
    public var mLeftEyeValue : Float = 0.0f
    public var mRightEyeValue : Float = 0.0f

    fun detectFaces(context: Context, picture: Bitmap): Bitmap {

        val detector = FaceDetector.Builder(context)
                .setTrackingEnabled(false)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build()

        val frame = Frame.Builder().setBitmap(picture).build()
        val faces = detector.detect(frame)
        Log.d(TAG, "detectFaces: number of faces = " + faces.size())

        var resultBitmap = picture

        if (faces.size() == 0) {
            Toast.makeText(context, R.string.no_faces_message, Toast.LENGTH_SHORT).show()

        } else {
            for (i in 0 until faces.size()) {
                val face = faces.valueAt(i)
                var emojiBitmap: Bitmap?

                when (whichEmoji(face)) {
                    // 스마일
                    Emoji.SMILE -> {
                        Log.d(TAG, ">> It's Smile!!")
                        emojiBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.smile)

                    }
                    // 무표정
                    Emoji.FROWN -> {
                        Log.d(TAG, ">> It's FROWN!!")
                        emojiBitmap = BitmapFactory . decodeResource (context.resources, R.drawable.frown)
                    }
                    // 눈감았지만, 웃는 표정
                    Emoji.CLOSED_EYE_SMILE -> {
                        Log.d(TAG, ">> It's Closed Eye But Smile!!")
                        emojiBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.closed_frown)
                    }
                    // 알 수 없는 표정정 (눈 모두 감은 표정)
                    else ->
                    {
                        Log.d(TAG, ">> It's Wondering!!")
                        emojiBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.wondering)
                        Toast.makeText(context, R.string.no_emoji, Toast.LENGTH_SHORT).show()
                    }
                }

                resultBitmap = addBitmapToface(resultBitmap, emojiBitmap, face)
            }
        }
        detector.release()
        return resultBitmap
    }

    private fun whichEmoji(face: Face): Emoji {

        //Log all the probabilities
        Log.d(TAG, ">> whichEmoji(): smilingProb = " + face.isSmilingProbability)
        Log.d(TAG, ">> whichEmoji(): leftEyeOpenProb = " + face.isLeftEyeOpenProbability)
        Log.d(TAG, ">> whichEmoji(): rightEyeOpenProb = " + face.isRightEyeOpenProbability)

        // 스마일, 왼쪽, 오른쪽 눈의 확률값 전역 변수에 저장하기
        mSmileValue = face.isSmilingProbability
        mLeftEyeValue = face.isLeftEyeOpenProbability
        mRightEyeValue = face.isRightEyeOpenProbability

        val smiling = face.isSmilingProbability > SMILING_PROB_THRESHOLD
        val leftEyeClosed = face.isLeftEyeOpenProbability < EYE_OPEN_PROB_THRESHOLD
        val rightEyeClosed = face.isRightEyeOpenProbability < EYE_OPEN_PROB_THRESHOLD

        //Detrmine and log the appropriate emoji
        val emoji: Emoji
        emoji = if (smiling) {
            if (leftEyeClosed && !rightEyeClosed) {
                Emoji.LEFT_WINK

            } else if (rightEyeClosed && !leftEyeClosed) {
                Emoji.RIGHT_WINK

            } else if (leftEyeClosed) {
                Emoji.CLOSED_EYE_SMILE

            } else {
                Emoji.SMILE

            }
        } else {
            if (leftEyeClosed && !rightEyeClosed) {
                Emoji.LEFT_WINK_FROWN
            } else if (rightEyeClosed && !leftEyeClosed) {
                Emoji.RIGHT_WINK_FROWN
            } else if (leftEyeClosed) {
                Emoji.CLOSED_EYE_FROWN
            } else {
                Emoji.FROWN
            }
        }
        // Log the chosen Emoji
        Log.d(TAG, ">> whichEmoji: " + emoji.name)

        // return the chosen Emoji
        return emoji
    }

    private fun addBitmapToface(backgroundBitmap: Bitmap, emojiBitmap: Bitmap?, face: Face): Bitmap {
        // Initialize the results bitmap to be a mutable copy of the original image
        var emojiBitmap = emojiBitmap
        val resultBitmap = Bitmap.createBitmap(backgroundBitmap.width,
                backgroundBitmap.height, backgroundBitmap.config)

        // Scale the emoji so it looks better on the face
        val scaleFactor = EMOJI_SCALE_FACTOR

        // Determine the size of the emoji to match the width of the face and preserve aspect ratio
        val newEmojiWidth = (face.width * scaleFactor).toInt()
        val newEmojiHeight = (emojiBitmap!!.height *
                newEmojiWidth / emojiBitmap.width * scaleFactor).toInt()


        // Scale the emoji
        emojiBitmap = Bitmap.createScaledBitmap(emojiBitmap, newEmojiWidth, newEmojiHeight, false)

        // Determine the emoji position so it best lines up with the face
        val emojiPositionX = face.position.x + face.width / 2 - emojiBitmap.width / 2
        val emojiPositionY = face.position.y + face.height / 2 - emojiBitmap.height / 3

        // Create the canvas and draw the bitmaps to it
        val canvas = Canvas(resultBitmap)
        canvas.drawBitmap(backgroundBitmap, 0f, 0f, null)
        canvas.drawBitmap(emojiBitmap, emojiPositionX, emojiPositionY, null)

        return resultBitmap
    }

    private enum class Emoji {
        SMILE,
        FROWN,
        LEFT_WINK,
        RIGHT_WINK,
        LEFT_WINK_FROWN,
        RIGHT_WINK_FROWN,
        CLOSED_EYE_SMILE,
        CLOSED_EYE_FROWN
    }
}