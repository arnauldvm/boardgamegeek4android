package com.boardgamegeek.model;

import com.boardgamegeek.io.AuthException;
import com.boardgamegeek.io.InvalidIdException;
import com.boardgamegeek.io.PossibleSuccessException;
import com.boardgamegeek.util.StringUtils;

import retrofit.RetrofitError;
import retrofit.converter.ConversionException;

public class PlayPostResponse {
	@SuppressWarnings("unused") private String html;
	@SuppressWarnings("unused") private String numplays;
	@SuppressWarnings("unused") private String playid;
	@SuppressWarnings("unused") private String error;

	private Exception mException;

	public PlayPostResponse(String errorMessage) {
		this.error = errorMessage;
	}

	public PlayPostResponse(Exception e) {
		mException = e;
	}

	public boolean hasError() {
		return getErrorMessage() != null;
	}

	public boolean hasAuthError() {
		if ("You must login to save plays".equals(error)) {
			return true;
		} else if (mException != null &&
			mException instanceof RetrofitError &&
			mException.getCause() instanceof ConversionException &&
			mException.getCause().getCause() instanceof AuthException) {
			return true;
		}
		return false;
	}

	public boolean hasInvalidIdError() {
		if ("You are not permitted to edit this play.".equals(error)) {
			return true;
		} else if ("Play does not exist.".equals(error)) {
			return true;
		} else if (mException != null &&
			mException instanceof RetrofitError &&
			mException.getCause() instanceof ConversionException &&
			mException.getCause().getCause() instanceof InvalidIdException) {
			return true;
		}
		return false;
	}

	public boolean hasPossibleSuccessError() {
		if (mException != null &&
			mException instanceof RetrofitError &&
			mException.getCause() instanceof ConversionException &&
			mException.getCause().getCause() instanceof PossibleSuccessException) {
			return true;
		}
		return false;
	}

	public String getErrorMessage() {
		if (mException != null) {
			if (hasPossibleSuccessError()) {
				return null;
			}
			return mException.getMessage();
		}
		return null;
	}

	public int getPlayCount() {
		if (hasError()) {
			return 0;
		} else {
			int start = html.indexOf(">");
			int end = html.indexOf("<", start);
			return StringUtils.parseInt(html.substring(start + 1, end), 1);
		}
	}
}
