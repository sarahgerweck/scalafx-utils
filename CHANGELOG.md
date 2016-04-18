# ScalaFX-Utils changelog

For a full change history, see the Git history. Only changes likely to affect
end users will be listed here.

### 0.6.0

   * Add new operations to flatten observable collections.
   * Updates to several dependencies.
      * This includes Shapeless 2.3.0, which may introduce binary
        incompatibilities if you have compiled against a different version.

### 0.7.0

   * Modified Scalaz instance to add `ReadOnlyObjectProperty` instances and
     improve specificity of the `ObservableValue[A, _]` instances.
