package org.jetbrains.plugins.javaFX.fxml.refs;

import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.completion.XmlTagInsertHandler;
import com.intellij.codeInsight.daemon.QuickFixActionRegistrar;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.quickfix.UnresolvedReferenceQuickFixProvider;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.xml.TagNameReference;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.javaFX.fxml.JavaFxPsiUtil;
import org.jetbrains.plugins.javaFX.fxml.descriptors.JavaFxClassBackedElementDescriptor;

/**
 * User: anna
 * Date: 1/8/13
 */
public class JavaFxTagNameReference extends TagNameReference{
  private static final Logger LOGGER = Logger.getInstance("#" + JavaFxTagNameReference.class.getName());

  public JavaFxTagNameReference(ASTNode element, boolean startTagFlag) {
    super(element, startTagFlag);
  }

  @Override
  public TextRange getRangeInElement() {
    final TextRange rangeInElement = super.getRangeInElement();
    final XmlTag tagElement = getTagElement();
    if (tagElement != null) {
      final String tagElementName = tagElement.getName();
      final int dotIdx = tagElementName.indexOf(".");
      if (dotIdx > -1 && dotIdx + 2 < rangeInElement.getEndOffset()) {
        return new TextRange(rangeInElement.getStartOffset() + dotIdx + 1, rangeInElement.getEndOffset());
      }
    }
    return rangeInElement;
  }

  @Override
  public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
    if (element instanceof PsiClass) {
      final String qualifiedName = ((PsiClass)element).getQualifiedName();
      if (qualifiedName != null) {
        final String shortName = StringUtil.getShortName(qualifiedName);
        final XmlTag tagElement = getTagElement();
        if (tagElement != null) {
          final String oldTagName = tagElement.getName();
          if (oldTagName.contains(".")) {
            return tagElement.setName(qualifiedName);
          }
          return tagElement.setName(shortName);
        }
        return getElement();
      }
    }
    return super.bindToElement(element);
  }

  @NotNull
  @Override
  public LookupElement[] getVariants() {
    if(!myStartTagFlag){
      return super.getVariants();
    }
    return LookupElement.EMPTY_ARRAY;
  }

  public static class JavaFxUnresolvedTagRefsProvider extends UnresolvedReferenceQuickFixProvider<JavaFxTagNameReference> {
    @Override
    public void registerFixes(JavaFxTagNameReference ref, QuickFixActionRegistrar registrar) {
      XmlTag element = ref.getTagElement();
      if (element != null) {
        registrar.register(new JavaFxImportClassFix(ref, element) {
          @Override
          protected XmlTag getTagElement(JavaFxTagNameReference ref) {
            return ref.getTagElement();
          }
        });
      }
    }

    @NotNull
    @Override
    public Class<JavaFxTagNameReference> getReferenceClass() {
      return JavaFxTagNameReference.class;
    }
  }
}
