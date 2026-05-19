package com.zergatul.scripting.tests.utility;

import com.zergatul.scripting.ErrorCode;

public record MarkedDiagnostic(String mark, ErrorCode errorCode, Object... parameters) {}