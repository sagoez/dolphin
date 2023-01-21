// Copyright (c) 2022 by LapsusHQ
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package dolphin.internal.syntax

private[dolphin] trait AllSyntax extends FutureSyntax with ResultSyntax
private[dolphin] object all      extends AllSyntax
